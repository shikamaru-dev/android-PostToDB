package local.hal.st31.post2db70321;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;


public class MainActivity extends AppCompatActivity {

    private static final String ACCESS_URL = "http://hal.architshin.com/st31/post2DB.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void sendButtonClick(View view) {
        EditText etLastName = findViewById(R.id.etLastName);
        EditText etFirstName = findViewById(R.id.etFirstName);
        EditText etStudentId = findViewById(R.id.etStudentId);
        EditText etSeatNo = findViewById(R.id.etSeatNo);
        EditText etMessage = findViewById(R.id.etMessage);

        TextView tvResult = findViewById(R.id.tvResult);

        tvResult.setText("");

        String lastname = etLastName.getText().toString();
        String firstname = etFirstName.getText().toString();
        String studentid = etStudentId.getText().toString();
        String seatno = etSeatNo.getText().toString();
        String message = etMessage.getText().toString();

        PostAccess access = new PostAccess(tvResult);
        access.execute(ACCESS_URL, lastname, firstname, studentid, seatno, message);
    }

    private class PostAccess extends AsyncTask<String, String, String> {
        private static final String DEBUG_TAG = "PostAccess";

        private TextView _tvResult;

        private boolean _success = false;

        public PostAccess(TextView tvResult) {

            _tvResult = tvResult;
        }

        @Override
        public String doInBackground(String... params) {
            String urlStr = params[0];
            String lastname = params[1];
            String firstname = params[2];
            String studentid = params[3];
            String seatno = params[4];
            String message = params[5];

            String postData = "lastname=" + lastname + "&firstname=" + firstname +
                    "&studentid=" + studentid + "&seatno=" + seatno + "&message=" + message;
            HttpURLConnection con = null;
            InputStream is = null;
            String result = "";

            try {
                URL url = new URL(urlStr);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.setDoOutput(true);
                OutputStream os = con.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();
                is = con.getInputStream();
                result = is2String(is);
                _success = true;

            } catch (SocketTimeoutException ex) {
                publishProgress(getString(R.string.msg_err_timeout));
                Log.e(DEBUG_TAG, "タイムアウト", ex);
            } catch (MalformedURLException ex) {
                publishProgress(getString(R.string.msg_err_send));
                Log.e(DEBUG_TAG, "URL変換失敗", ex);
            } catch (IOException ex) {
                publishProgress(getString(R.string.msg_err_send));
                Log.e(DEBUG_TAG, "通信失敗", ex);
            } finally {
                if (con != null) {
                    con.disconnect();
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException ex) {
                    publishProgress(getString(R.string.msg_err_parse));
                    Log.e(DEBUG_TAG, "InputStream開放失敗", ex);
                }
            }
            return result;
        }

        @Override
        public void onPostExecute(String result) {
            if (_success) {
                String name = "";
                String studentid = "";
                String seatno = "";
                String status = "";
                String msg = "";
                String serialno = "";
                String timestamp = "";
                try {
                    JSONObject rootJSON = new JSONObject(result);
                    name = rootJSON.getString("name");
                    studentid = rootJSON.getString("studentid");
                    seatno = rootJSON.getString("seatno");
                    status = rootJSON.getString("status");
                    msg = rootJSON.getString("msg");
                    serialno = rootJSON.getString("serialno");
                    timestamp = rootJSON.getString("timestamp");
                } catch (JSONException ex) {
                    Log.e(DEBUG_TAG, "JSON解析失敗", ex);
                }

                String message = getString(R.string.dlg_msg_name) + name + "\n" + getString(R.string.dlg_msg_student_id) + studentid +
                        "\n" + getString(R.string.dlg_msg_seat_no) + seatno + "\n" + getString(R.string.dlg_msg_status) + status + "\n"
                        + getString(R.string.dlg_msg_message) + msg + "\n" + getString(R.string.dlg_serial_no) + serialno + "\n" + getString(R.string.dlg_timestamp) + timestamp + "\n";
                _tvResult.setText(message);

            }
        }

        private String is2String(InputStream is) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuffer sb = new StringBuffer();
            char[] b = new char[1024];
            int line;
            while (0 <= (line = reader.read(b))) {
                sb.append(b, 0, line);
            }
            return sb.toString();
        }
    }

}
