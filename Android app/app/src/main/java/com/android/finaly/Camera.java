package com.android.finaly;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class Camera {

    private final int GET_TOKEN = 257;
    private final int ENABLE_STREAMING = 259;
    private final int DISABLE_STREAMING = 260;
    private final int TAKE_PHOTO = 769;
    private final int STATUS_UPDATE = 7;
    private final int GET_BATTERY_LEVEL = 13;
    private final int START_RECORDING = 513;
    private final int STOP_RECORDING = 514;
    private final int LISTING = 1282;
    private final int CHANGE_DIR = 1283;


    static final String STREAM_IP = "rtsp://192.168.42.1/live";
    static final String DEFAULT_IP = "192.168.42.1";
    static final int DEFAULT_PORT = 7878;


    private int token;
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private boolean streamEnabled;
    private boolean isRecording;
    private Logger log;
    private Queue<JSONObject> unhandledPackets;

    public Camera(String ip, int port) throws IOException {
        Pattern checkIP = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");
        if (checkIP.matcher(ip).matches() && port > 999 && port < 10000) {
            socket = new Socket(ip, port);
        } else throw new IOException();
        in = socket.getInputStream();
        out = socket.getOutputStream();
        streamEnabled = false;
        isRecording = false;
        log = Logger.getLogger(Camera.class.getName());
        unhandledPackets = new LinkedList<>();
    }

    private void send(JSONObject jo) throws IOException {
        out.write(jo.toString().getBytes());
        Log.i("send", jo.toString());
    }

    private JSONObject receive() throws IOException, JSONException {
        JSONObject jo;
        if (unhandledPackets.size() >0 ) {
            jo = unhandledPackets.peek();
            unhandledPackets.remove();
            return jo;
        }
        while (in.available()==0);
        byte[] buffer = new byte[in.available()];
        int length = in.read(buffer);
        int offset = 0;
        for (int i = 1; i < length; i++) {
            if (buffer[i-1] == '}' && buffer[i] == '{') {
                unhandledPackets.add(new JSONObject(new String(buffer, offset, i-offset)));
                offset = i;
            }
        }
        unhandledPackets.add(new JSONObject(new String(buffer, offset, buffer.length-offset)));
        jo = unhandledPackets.peek();
        unhandledPackets.remove();
        Log.i("receive", Objects.requireNonNull(jo).toString());
        return jo;
    }

    public void start() {
        try {
            send(new JSONObject().put("msg_id", GET_TOKEN).put("token", 0));
            JSONObject jo;
            do {
                jo = receive();
                if ((Integer) jo.get("msg_id") == STATUS_UPDATE) {
                    handleStatusUpdate(jo);
                }
            } while ((Integer) jo.get("msg_id") != GET_TOKEN);
            token = (Integer) jo.get("param");
        } catch (IOException e) {
            Log.e("start", "Send/receive error");
            Log.d("start", e.toString());
        } catch (JSONException e) {
            Log.e("start", "Unexpected JSON Object");
            Log.d("start", e.toString());
        }
    }

    private void handleStatusUpdate(JSONObject jo) {
        try {
            String type = (String) jo.get("type");
            if (type.equals("battery")) {
                Log.i("handleStatusUpdate", "Status update: battery level - " + jo.get("param"));
            }
            else {
                Log.w("handleStatusUpdate", "Unknown type of status update.");
            }
        } catch (JSONException e) {
            Log.e("handleStatusUpdate", "Unexpected JSON Object");
            Log.d("handleStatusUpdate", e.toString());
        }
    }

    public void takePhoto() {
        try {
            send(new JSONObject().put("msg_id", TAKE_PHOTO).put("token", token));
            JSONObject jo;
            do {
                jo = receive();
                if ((Integer) jo.get("msg_id") == STATUS_UPDATE) {
                    handleStatusUpdate(jo);
                }
            } while ((Integer) jo.get("msg_id") != TAKE_PHOTO);
            if ((Integer) jo.get("rval") != 0) {
                // TODO: 4/23/2019
                return;
            }
            do {
                jo = receive();
                if ((Integer) jo.get("msg_id") == STATUS_UPDATE) {
                    handleStatusUpdate(jo);
                }
            } while (!jo.get("type").equals("vf_start"));
            streamEnabled = true;
        } catch (IOException e) {
            Log.e("takePhoto", "Send/receive error");
            Log.d("takePhoto", e.toString());
        } catch (JSONException e) {
            Log.e("takePhoto", "Unexpected JSON Object");
            Log.d("takePhoto", e.toString());
        }
    }

    public void enableStreaming() {
        try {
            if (streamEnabled) return;
            send(new JSONObject().put("msg_id", ENABLE_STREAMING).put("token", token));
            JSONObject jo;
            do {
                jo = receive();
                if ((Integer) jo.get("msg_id") == STATUS_UPDATE) {
                    handleStatusUpdate(jo);
                }
            } while ((Integer) jo.get("msg_id") != ENABLE_STREAMING);
            if ((Integer) jo.get("rval") != 0) {
                // TODO: 4/23/2019
                return;
            }
            do {
                jo = receive();
                if ((Integer) jo.get("msg_id") == STATUS_UPDATE) {
                    handleStatusUpdate(jo);
                }
            } while (!jo.get("type").equals("vf_start"));
            streamEnabled = true;
        } catch (IOException e) {
            Log.e("enableStreaming", "Send/receive error");
            Log.d("enableStreaming", e.toString());
        } catch (JSONException e) {
            Log.e("enableStreaming", "Unexpected JSON Object");
            Log.d("enableStreaming", e.toString());
        }
    }

    public void disableStreaming() {
        try {
            if (!streamEnabled) return;
            send(new JSONObject().put("msg_id", DISABLE_STREAMING).put("token", token));
            JSONObject jo;
            do {
                jo = receive();
                if ((Integer) jo.get("msg_id") == STATUS_UPDATE) {
                    handleStatusUpdate(jo);
                }
            } while ((Integer) jo.get("msg_id") != DISABLE_STREAMING);
            if ((Integer) jo.get("rval") != 0) {
                // TODO: 4/23/2019
                return;
            }
            do {
                jo = receive();
                if ((Integer) jo.get("msg_id") == STATUS_UPDATE) {
                    handleStatusUpdate(jo);
                }
            } while (!jo.get("type").equals("vf_stop"));
            streamEnabled = false;
        } catch (IOException e) {
            Log.e("disableStreaming", "Send/receive error");
            Log.d("disableStreaming", e.toString());
        } catch (JSONException e) {
            Log.e("disableStreaming", "Unexpected JSON Object");
            Log.d("disableStreaming", e.toString());
        }
    }

    public void startRecording() {
        try {
            if (isRecording) return;
            send(new JSONObject().put("msg_id", START_RECORDING).put("token", token));
            JSONObject jo;
            do {
                jo = receive();
                if ((Integer) jo.get("msg_id") == STATUS_UPDATE) {
                    handleStatusUpdate(jo);
                }
            } while ((Integer) jo.get("msg_id") != START_RECORDING);
            if ((Integer) jo.get("rval") != 0) {
                // TODO: 4/23/2019
                return;
            }
            do {
                jo = receive();
                if ((Integer) jo.get("msg_id") == STATUS_UPDATE) {
                    handleStatusUpdate(jo);
                }
            } while ((streamEnabled) ? !jo.get("type").equals("vf_stop") : !jo.get("type").equals("start_video_record"));
            streamEnabled = false;
            isRecording = true;
        } catch (IOException e) {
            Log.e("startRecording", "Send/receive error");
            Log.d("startRecording", e.toString());
        } catch (JSONException e) {
            Log.e("startRecording", "Unexpected JSON Object");
            Log.d("startRecording", e.toString());
        }
    }

    public void stopRecording() {
        try {
            if (!isRecording) return;
            send(new JSONObject().put("msg_id", STOP_RECORDING).put("token", token));
            JSONObject jo;
            do {
                jo = receive();
                if ((Integer) jo.get("msg_id") == STATUS_UPDATE) {
                    handleStatusUpdate(jo);
                }
            } while ((Integer) jo.get("msg_id") != STOP_RECORDING);
            if ((Integer) jo.get("rval") != 0) {
                // TODO: 4/23/2019
                return;
            }
            do {
                jo = receive();
                if ((Integer) jo.get("msg_id") == STATUS_UPDATE) {
                    handleStatusUpdate(jo);
                }
            } while (!jo.get("type").equals("vf_start"));
            streamEnabled = true;
            isRecording = false;
        } catch (IOException e) {
            Log.e("stopRecording", "Send/receive error");
            Log.d("stopRecording", e.toString());
        } catch (JSONException e) {
            Log.e("stopRecording", "Unexpected JSON Object");
            Log.d("stopRecording", e.toString());
        }
    }

    public String[] listing() {
        ArrayList<String> result = new ArrayList<>();
        String[] result_array;
        try {
            send(new JSONObject().put("msg_id", LISTING).put("token", token));
            JSONObject jo;
            do {
                jo = receive();
                if ((Integer) jo.get("msg_id") == STATUS_UPDATE) {
                    handleStatusUpdate(jo);
                }
            } while ((Integer) jo.get("msg_id") != LISTING);
            if ((Integer) jo.get("rval") != 0) {
                // TODO: 4/23/2019
                result_array = new String[result.size()];
                result.toArray(result_array);
                return result_array;
            }
            JSONArray ja = (JSONArray) jo.get("listing");
            for (int i = 0; i < ja.length(); i++) {
                String dir = ((JSONObject) ja.get(i)).keys().next();
                result.add(dir);
                Log.v("listing", dir);
            }
        } catch (IOException e) {
            Log.e("testing", "Send/receive error");
            Log.d("testing", e.toString());
        } catch (JSONException e) {
            Log.e("testing", "Unexpected JSON Object");
            Log.d("testing", e.toString());
        }
        result_array = new String[result.size()];
        result.toArray(result_array);
        return result_array;
    }

    public boolean changeDirectory(String dir) {
        try {
            send(new JSONObject().put("msg_id", CHANGE_DIR).put("param", dir).put("token", token));
            JSONObject jo;
            do {
                jo = receive();
                if ((Integer) jo.get("msg_id") == STATUS_UPDATE) {
                    handleStatusUpdate(jo);
                }
            } while ((Integer) jo.get("msg_id") != CHANGE_DIR);
            if ((Integer) jo.get("rval") != 0) {
                // TODO: 4/23/2019
                return false;
            }
        } catch (IOException e) {
            Log.e("testing", "Send/receive error");
            Log.d("testing", e.toString());
        } catch (JSONException e) {
            Log.e("testing", "Unexpected JSON Object");
            Log.d("testing", e.toString());
        }
        return true;
    }

    public String getSTREAM_IP() {
        return STREAM_IP;
    }

    public boolean isRecording() {
        return isRecording;
    }
}
