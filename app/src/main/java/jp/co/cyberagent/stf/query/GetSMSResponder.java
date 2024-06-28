package jp.co.cyberagent.stf.query;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.google.protobuf.GeneratedMessageLite;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.co.cyberagent.stf.proto.Wire;

public class GetSMSResponder extends AbstractResponder {
    private static final String TAG = GetPropertiesResponder.class.getSimpleName();

    public GetSMSResponder(Context context) {
        super(context);
    }

    private String extractOtp(String message, Integer length) {
        // Regular expression to find a number
        String regex = "\\b\\d{" + length + "}\\b";
        Pattern otpPattern = Pattern.compile(regex);
        Matcher matcher = otpPattern.matcher(message);

        if (matcher.find()) {
            return matcher.group(0);  // Return the OTP
        }
        return null;  // No OTP found
    }

    private String readSMSBySender(String sender, Integer length) {
        StringBuilder result = new StringBuilder();
        Uri smsUri = Uri.parse("content://sms/inbox");
        Cursor cursor = context.getContentResolver().query(smsUri, null, null, null, "date DESC");

        int count = 0;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (count > 5) {
                    break;
                }
                String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                Log.d(TAG, "Address: " + address);

                if (address.equals(sender)) {
                    String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));

                    String otp = extractOtp(body, length);

                    // long date = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
                    // convert date epoch to human readable format
                    // String dateStr = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm").format(new java.util.Date(date));
                    // result.append("SMS from ").append(address).append(" at ").append(dateStr).append(": ").append(otp).append("\n");
                    result.append(otp);
                    break;
                }
                count += 1;
            }
            cursor.close();
        }
        Log.d(TAG, "SMS: " + result);
        return result.toString();
    }

    @Override
    public GeneratedMessageLite respond(Wire.Envelope envelope) throws InvalidProtocolBufferException {
        Wire.GetSMSRequest request = Wire.GetSMSRequest.parseFrom(envelope.getMessage());
        String sender = request.getSenderNumber();
        Integer length = request.getOtpLength();
        String smsContent = this.readSMSBySender(sender, length);

        return Wire.GetSMSResponse.newBuilder()
                .setSuccess(true)
                .setBody(smsContent)
                .build();
    }

    @Override
    public void cleanup() {

    }
}
