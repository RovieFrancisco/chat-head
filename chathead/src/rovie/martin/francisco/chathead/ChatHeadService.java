package rovie.martin.francisco.chathead;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.os.Build;
import android.util.DisplayMetrics;
import android.widget.TextView;
import android.graphics.PorterDuff;

public class ChatHeadService extends Service {

    private WindowManager windowManager;
    private View chatHeadView;
    private DisplayMetrics displayMetrics;
    private TextView txt; //debugging purpose
    private ImageView img;

    public ChatHeadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Deprecated
    @Override
    public void onCreate() {
        super.onCreate();
        chatHeadView = LayoutInflater.from(this).inflate(R.layout.activity_head, null);
        displayMetrics = new DisplayMetrics();

        txt = chatHeadView.findViewById(R.id.xy); //debugging purpose

        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        else
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            LAYOUT_FLAG,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.RIGHT;        //Initially view will be added to top-left corner
        params.x = 0;
        params.y = 100;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(chatHeadView, params);

        final ImageView chatHeadImage = (ImageView) chatHeadView.findViewById(R.id.head_profile);
        chatHeadImage.setOnTouchListener(new View.OnTouchListener() {
            private int lastAction;
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;

                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();

                        lastAction = event.getAction();
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (lastAction == MotionEvent.ACTION_DOWN) {
                            Intent intent = new Intent(ChatHeadService.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                            stopSelf();
                        }
                        
                        if (params.x >= getXY(true)-50 && params.x <= getXY(true)+50 && params.y >= getXY(false)-50 && params.y <= getXY(false)+50)
                            stopSelf();

                        if (params.x - 1 < getXY(true)) {
                            params.x = 0;
                            windowManager.updateViewLayout(chatHeadView, params);
                        }
                        else if (params.x + 1 > getXY(true)) {
                            params.x = getXY(true) * 2;
                            windowManager.updateViewLayout(chatHeadView, params);
                        }
                        
                        lastAction = event.getAction();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = getScreenSize(initialX - (int) (event.getRawX() - initialTouchX), true);
                        params.y = getScreenSize(initialY + (int) (event.getRawY() - initialTouchY), false);

                        txt.setText(params.x + " | " + params.y); //debugging purpose
                        if (params.x >= getXY(true)-50 && params.x <= getXY(true)+50 && params.y >= getXY(false)-50 && params.y <= getXY(false)+50) {
                            txt.setTextColor(0xFFFF0000); //debugging purpose
                            chatHeadImage.setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
                        }
                        else {
                            txt.setTextColor(0xFF000000);
                            chatHeadImage.clearColorFilter();
                        }

                        windowManager.updateViewLayout(chatHeadView, params);
                        lastAction = event.getAction();
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatHeadView != null) windowManager.removeView(chatHeadView);
    }

    public int getXY(boolean X) {
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels - 134;
        int width = displayMetrics.widthPixels - 120;
        
        if (X)
            return width / 2;
        else
            return height;
    }
    public int getScreenSize(int coor, boolean X) {
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels - 134;
        int width = displayMetrics.widthPixels - 120;
        
        if (X) {
            if (coor < 0)
                coor = 0;
            else if (coor > width)
                coor = width;
        }
        else {
            if (coor < 0)
                coor = 0;
            else if (coor > height)
                coor = height;
        }

        return coor;
    }
}