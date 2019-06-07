package MyChattingProgram;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;

public class DBManager {

    private final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver"; //드라이버
    private final String DB_URL = "jdbc:mysql://127.0.0.1:3306/myproject?characterEncoding=UTF-8&serverTimezone=UTC"; //접속할 DB 서버

    private final String USER_NAME = "user1"; //DB에 접속할 사용자 이름을 상수로 정의
    private final String PASSWORD = "user1"; //사용자의 비밀번호를 상수로 정의

    Connection conn = null;
    Statement state = null;
    ResultSet resultSet = null;

    public DBManager() {
    }

    public void closeDB() {

        try {
            if(conn != null) {
                conn.close();
            }
            if(state != null) {
                state.close();
            }
            if(resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    public int insertMessages(String roomId, String userId, String msg, String msg_type) {

        int resultValue = 0;

        try {
//            Class.forName(JDBC_DRIVER);

            conn = DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
            state = conn.createStatement();


            String sql = "INSERT INTO message (room_id, user_id, message, message_type) " +
                    " VALUES (" + roomId + ", '" + userId + "', '" + msg + "', '" + msg_type + "')";

            resultValue = state.executeUpdate(sql);


        } catch (SQLException e) {
                e.printStackTrace();
        }
        return resultValue;

        }

        // invited_user ->(String)user*user1
    public int addUserInChatRoom(String roomId, String array) {

        int resultValue = 0;

        try {

            conn = DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
            state = conn.createStatement();

            try {
                JSONArray jsonArray = new JSONArray(array);

                for(int i = 0 ; i < jsonArray.length(); i++) {

                    JSONObject object = jsonArray.getJSONObject(i);
                    String id = object.getString("id");
                    String user_id = object.getString("userId");

                    String sql = "INSERT INTO participants (room_id, user_id) " +
                            " VALUES (" + roomId + ", '" + id + "')";
                    resultValue = state.executeUpdate(sql);

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultValue;

    }
}

