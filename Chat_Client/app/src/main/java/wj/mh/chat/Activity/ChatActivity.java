/*
 MultiChatClient Modified by ihjung, Hansung University.
 11/22 한글 I/O 문제 해결
 */
package wj.mh.chat.Activity;

import android.app.Notification;
import android.app.Notification.InboxStyle;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import wj.mh.chat.Adapter.EmoticonsGridAdapter;
import wj.mh.chat.Adapter.EmoticonsPagerAdapter;
import wj.mh.chat.Adapter.MyAdapter;
import wj.mh.chat.ChatData.ChatMessage;
import wj.mh.chat.R;
//import wj.mh.chat.ChatData.MyArray;

public class ChatActivity extends FragmentActivity implements EmoticonsGridAdapter.KeyClickListener {
    /////////////////////////////////////////////////////
    //이모티콘 관련 변수들
    /////////////////////////////////////////////////////

    private static final int NO_OF_EMOTICONS = 54;
    private PopupWindow popupWindow;
    private View popUpView;

    private boolean isKeyBoardVisible;
    private int keyboardHeight;
    private LinearLayout emoticonsCover;

    private Bitmap[] emoticons;
    private LinearLayout parentLayout;

    /////////////////////////////////////////////////////
    View loginView, logoutView;
    public EditText chat_txt;// chat_list;
    public ListView chat_list;
    public ImageButton send_btn, plus_btn;
//    private ImageView mPhotoImageView;

    private ArrayAdapter<String> dataAdapter;
    private MyAdapter adapter;
    private ArrayList<ChatMessage> chatHistory;


    private String sex;
    private static String myID = "";
    private static String otherID = "";
    String r_msg;

    Intent intent;

    Socket socket;
    final String ip = "197.168.100.24"; //genymotion host
    final int port = 44444; // port number

    private InputStream is;
    private OutputStream os;
    private DataInputStream dis;
    private DataOutputStream dos;
    static Bitmap bitmap = null;


    private boolean status;
    private Thread thread;

    //Notification  = 알림
    NotificationManager nm;
    PendingIntent pendingIntent;
    Notification.Builder mBuilder;
    Notification.InboxStyle style;
    int mcnt = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

//        MyArray.myData.add("어서오세요.");

        popUpView = getLayoutInflater().inflate(R.layout.emoticons_popup, null);
        emoticonsCover = (LinearLayout) findViewById(R.id.footer_for_emoticons);

        loginView = findViewById(R.id.login_view);
        logoutView = findViewById(R.id.logout_view);
        chat_txt = (EditText) findViewById(R.id.chat_txt);
        chat_list = (ListView) findViewById(R.id.chat_list);
        send_btn = (ImageButton) findViewById(R.id.send_btn);
        plus_btn = (ImageButton) findViewById(R.id.plus_btn);
        parentLayout = (LinearLayout) findViewById(R.id.list_parent);

        chatHistory = new ArrayList<ChatMessage>();

        //성별 값을 가져온다. ( 남 : 0 / 여 : 1 )
        intent = getIntent();
        sex = getIntent().getStringExtra("sex");

        notificationInit();
        connectServer();
        initControls();

        readEmoticons();
        enablePopUpView();
        checkKeyboardHeight(parentLayout);
    }

    private void initControls() {

        chatHistory = new ArrayList<ChatMessage>();

        ChatMessage msg = new ChatMessage();
        msg.setMe(false);
        msg.setMessage("반갑습니다.\n위의 랜덤채팅시작 버튼을 눌러 채팅을 시작 해 주세요.");
        msg.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatHistory.add(msg);

        adapter = new MyAdapter(ChatActivity.this, new ArrayList<ChatMessage>());
        chat_list.setAdapter(adapter);
        send_btn.setClickable(false);


        for (int i = 0; i < chatHistory.size(); i++) {
            ChatMessage message = chatHistory.get(i);
            displayMessage(message);
        }
    }

    // notification 초기화
    public void notificationInit() {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder = new Notification.Builder(this);
        mBuilder.setSmallIcon(R.drawable.ic_launcher);
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setContentTitle("ChatActivity");
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setAutoCancel(true);
        mBuilder.setDeleteIntent(pendingIntent); //�끂�떚 �꽑�깮 �떆 �궗�씪吏�寃� �븿

        style = new InboxStyle(mBuilder);
    }

    /* �궡媛� �봽濡쒓렇�옩�쑝濡� �룎�븘�삤硫� 移댁슫�듃瑜� 0濡� �븯怨� �엯�젰�맂 媛믪쓣 �뾾�빊 ->硫붿떆吏�瑜� 吏��슦�뒗 Tip*/
    @Override
    protected void onResume() {
        super.onResume();
        if (mcnt != 0) {
            mcnt = 0;
            style = new InboxStyle(mBuilder);
        }

    }


    //종료 버튼을 클릭했을 때
    public void mLogoutClick(View view) {
        Toast.makeText(this, "대화 종료", Toast.LENGTH_SHORT).show();
        loginView.setVisibility(View.VISIBLE);
        logoutView.setVisibility(View.INVISIBLE);
        otherID = "";
        send_btn.setClickable(false);
        Log.d("test", "접속" + otherID);

        ChatMessage chatMessage2 = new ChatMessage();
        chatMessage2.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatMessage2.setMessage("대화가 종료되었습니다.");
        chatMessage2.setMe(true);
        displayMessage(chatMessage2);
//        chat_list.append("대화가 끝났습니다");
        /*chat_list.setText("");*/
//        MyArray.myData.add("대화가 끝났습니다.");

        send_Message("end-" + myID);
    }

    //시작 버튼을 클릭했을 때
    public void mLoginClick(View view) {
        Toast.makeText(this, "대화 시작", Toast.LENGTH_SHORT).show();
        //시작버튼을 클릭했을 때 chat_txt을 다 지워줘야함.
//        chat_list.setText("");
//        MyArray.myData.clear();
//        MyImageArray.myImgData.clear();
        MyAdapter.chatMessages.clear();


        loginView.setVisibility(View.INVISIBLE);
        logoutView.setVisibility(View.VISIBLE);

        if (!otherID.equals(""))
            send_btn.setClickable(true);
        send_Message("start-" + myID);
    }


    public void send_Message(byte[] bb) {        //메세지를 보내는 메서드

        try {
            dos.write(bb); //.writeUTF(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send_Message(String str) {        //메세지를 보내는 메서드
        try {
            byte[] bb;
            bb = str.getBytes("ksc5601");    //한글로 바꿔주는 작업
            dos.write(bb); //.writeUTF(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send_Message(Uri uri) {        //메세지를 보내는 메서드

        byte[] data = null;
        try {
            ContentResolver cr = getBaseContext().getContentResolver();
            InputStream inputStream = cr.openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            data = baos.toByteArray();

            dos.write(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send_Message(Bitmap bt) {        //메세지를 보내는 메서드
        try {
            byte[] bb;

            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            bt.compress(Bitmap.CompressFormat.JPEG, 100, byteArray);
            bb = byteArray.toByteArray();

            dos.write(bb);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Send 버튼을 클릭했을 때
    public void mSendClick(View view) {

        //메세지를 "to-myID-otherID-메세지내용" 으로 보내준다.
        send_Message("to-" + myID + "-" + otherID + "-" + chat_txt.getText());
        chat_txt.setText("");
    }

    // 서버에 연결하는 메서드. 스레드를 돌려 계속 통신하도록 해 준다.
    public void connectServer() {
        new Thread() {
            public void run() {
                try {
                    socket = new Socket(ip, port);
                    Log.d("[Client]", " Server connected !!");

                    //inMsg = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    //outMsg = new PrintWriter(socket.getOutputStream(),true);

                    //자바에서 한거랑 똑같으니 참고하기
                    is = socket.getInputStream();
                    dis = new DataInputStream(is);
                    os = socket.getOutputStream();
                    dos = new DataOutputStream(os);

                    //서버에 접속하면 성별을 보내는 메서드 호출
                    send_Message(sex);

                    thread = new Thread(new ReceiveMsg());
                    thread.setDaemon(true);
                    thread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("[ChatActivity]", " connectServer() Exception !!");
                }
            }
        }.start();

//        if (otherID.equals("")) {
//            send_btn.setEnabled(false);
//        } else {
//            send_btn.setEnabled(true);
//        }
    }

    //메세지 알림을 chat_list에 보여줌
    Handler mHandler = new Handler() {
        @SuppressWarnings("null")
        public void handleMessage(android.os.Message msg) {
            //여기서 서버에서 오는 메세지를 띄워줌
//            chat_list.append(r_msg + "\n");
//            myArray.setMyData(r_msg);

            if (!r_msg.equals("")) {
//                MyArray.myData.add(r_msg);
//                chat_list.setSelection(MyArray.myData.size() - 1);
                String[] token = r_msg.split("-");
                if (token[0].equals("I")) {
                    r_msg = r_msg.substring(2);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setMessage(r_msg);
                    chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                    chatMessage.setMe(true);
                    displayMessage(chatMessage);
                } else if (token[0].equals("YOU")) {
                    r_msg = r_msg.substring(4);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setMessage(r_msg);
                    chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                    chatMessage.setMe(false);
                    displayMessage(chatMessage);

                }
            }


            //상대에게 메세지가 오면 상단에 Notification을 띄워줌
//			if (r_msg.contains("[낯선사람] : "))
//				newNotification(r_msg);

            if (msg.what == 4) {
                Log.d("test", "test2");
                /*login_btn.performClick();*/
//                chat_list.append("대화가 끝났습니다");
//                MyArray.myData.add("대화가 끝났습니다.");

                ChatMessage chatMessage2 = new ChatMessage();
                chatMessage2.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                chatMessage2.setMessage("대화가 종료되었습니다.");
                chatMessage2.setMe(true);
                displayMessage(chatMessage2);
                loginView.setVisibility(View.VISIBLE);
                logoutView.setVisibility(View.INVISIBLE);
            }
        }
    };

    public void displayMessage(ChatMessage message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scroll();
    }

    private void scroll() {
        chat_list.setSelection(chat_list.getCount() - 1);
    }


    // 내부클래스로 서버에서 받은 베세지를 처리 해 주는 메서드
    class ReceiveMsg implements Runnable {

        @SuppressWarnings("null")
        @Override
        public synchronized void run() {
            status = true;
            while (status) {
                try {
                    byte[] b = new byte[4096];
                    dis.read(b);
                    String msg = new String(b, "euc-kr"); //+ "\n";

                    String myToken[];

                    // DEFINE-String	: myID에 String을 넣어준다
                    // IN-String			: otherID에 String을 넣어준다.
                    // OUT				: otherID에 null을 넣어준다.
                    myToken = msg.split("-");


                    if (myToken[0].equals("DEFINE")) {
                        myToken[1] = myToken[1].trim();
                        myID = myToken[1];
                        r_msg = "";
                        mHandler.sendEmptyMessage(0);
                    } else if (myToken[0].equals("IN")) {

                        myToken[1] = myToken[1].trim();
                        myToken[2] = myToken[2].trim();
                        myID = myToken[1];
                        otherID = myToken[2];
                        Log.d("test", "IN명령어" + myToken[1] + " " + myToken[2]);
                        if (!otherID.equals(""))
                            send_btn.setClickable(true);
                        r_msg = "";
                        mHandler.sendEmptyMessage(0);


                    } else if (myToken[0].equals("OUT")) {
                        myToken[1] = myToken[1].trim();

                        otherID = "";
                        r_msg = "";
                        Log.d("test", "test 들어옴");
                        /*login_btn.performClick();*/  //UI수정은 핸들러에서
                        /*chat_list.setText("");*/
                        mHandler.sendEmptyMessage(4); //핸들러에게 자동버튼 클릭(4번)
                        send_btn.setClickable(false);
                    } else if (myToken[0].equals("I") || myToken[0].equals("YOU")) {
                        r_msg = msg;
                        mHandler.sendEmptyMessage(0);
                    } else {
//                        bitmap = BitmapFactory.decodeStream(byteArrayIn);
                    }


                } catch (IOException e) {
                    //e.printStackTrace();
                    //status = false;
                    try {
                        os.close();
                        is.close();
                        dos.close();
                        dis.close();
                        socket.close();
                        break;
                    } catch (IOException e1) {
                        e.printStackTrace();
                    }
                }
            }
            Log.d("[MultiChatClient]", " Stopped");

        }
    }



    //이모티콘
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.plus_btn:

                if (!popupWindow.isShowing()) {

                    popupWindow.setHeight((int) (keyboardHeight));

                    if (isKeyBoardVisible) {
                        emoticonsCover.setVisibility(LinearLayout.GONE);
                    } else {
                        emoticonsCover.setVisibility(LinearLayout.VISIBLE);
                    }
                    popupWindow.showAtLocation(parentLayout, Gravity.BOTTOM, 0, 0);

                } else {
                    popupWindow.dismiss();
                }

                break;
        }
    }

    private void readEmoticons() {

        emoticons = new Bitmap[NO_OF_EMOTICONS];
        for (short i = 0; i < NO_OF_EMOTICONS; i++) {
            emoticons[i] = getImage((i + 1) + ".png");
        }
    }


    /**
     * Overriding onKeyDown for dismissing keyboard on key down
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * Checking keyboard height and keyboard visibility
     */
    int previousHeightDiffrence = 0;

    private void checkKeyboardHeight(final View parentLayout) {
        parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {

                        Rect r = new Rect();
                        parentLayout.getWindowVisibleDisplayFrame(r);

                        int screenHeight = parentLayout.getRootView()
                                .getHeight();
                        int heightDifference = screenHeight - (r.bottom);

                        if (previousHeightDiffrence - heightDifference > 50) {
                            popupWindow.dismiss();
                        }

                        previousHeightDiffrence = heightDifference;
                        if (heightDifference > 100) {

                            isKeyBoardVisible = true;
                            changeKeyboardHeight(heightDifference);

                        } else {

                            isKeyBoardVisible = false;

                        }

                    }
                });

    }

    /**
     * change height of emoticons keyboard according to height of actual
     * keyboard
     *
     * @param height minimum height by which we can make sure actual keyboard is
     *               open or not
     */
    private void changeKeyboardHeight(int height) {

        if (height > 100) {
            keyboardHeight = height;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, keyboardHeight);
            emoticonsCover.setLayoutParams(params);
        }

    }

    /**
     * Defining all components of emoticons keyboard
     */
    private void enablePopUpView() {

        ViewPager pager = (ViewPager) popUpView.findViewById(R.id.emoticons_pager);
        pager.setOffscreenPageLimit(3);

        ArrayList<String> paths = new ArrayList<String>();

        for (short i = 1; i <= NO_OF_EMOTICONS; i++) {
            paths.add(i + ".png");
        }


        EmoticonsPagerAdapter adapter = new EmoticonsPagerAdapter(ChatActivity.this, paths, this);
        pager.setAdapter(adapter);

        // Creating a pop window for emoticons keyboard
        popupWindow = new PopupWindow(popUpView, ViewGroup.LayoutParams.MATCH_PARENT,
                (int) keyboardHeight, false);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                emoticonsCover.setVisibility(LinearLayout.GONE);
            }
        });
    }

    /**
     * For loading smileys from assets
     */
    private Bitmap getImage(String path) {
        AssetManager mngr = getAssets();
        InputStream in = null;
        try {
            in = mngr.open("emoticons/" + path);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bitmap temp = BitmapFactory.decodeStream(in, null, null);
        return temp;
    }


    @Override
    public void keyClickedIndex(final String index) {

        Html.ImageGetter imageGetter = new Html.ImageGetter() {
            public Drawable getDrawable(String source) {
                StringTokenizer st = new StringTokenizer(index, ".");
                Drawable d = new BitmapDrawable(getResources(), emoticons[Integer.parseInt(st.nextToken()) - 1]);
                d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                return d;
            }
        };

        Spanned cs = Html.fromHtml("<img src ='" + index + "'/>", imageGetter, null);


        int cursorPosition = chat_txt.getSelectionStart();
        send_btn.performClick();
        chat_txt.getText().insert(cursorPosition, cs);
    }


}