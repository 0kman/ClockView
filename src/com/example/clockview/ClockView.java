package com.example.clockview;

import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class ClockView extends View {

    private boolean BOOLEAN_REFRESH_TIME = true;

	private Thread refreshThread;//ˢ��ʱ���߳�

    private float refresh_time = 1000;//����ˢ�µ�ʱ��


    private float width_circle = 5;//��������ԲȦ�Ŀ��
    private float width_longer = 5;//����̶ȿ��
    private float width_shorter = 3;//������̶ȿ��
    private float length_longer = 60;//����̶ȳ���
    private float length_shorter = 30;//������̶ȳ���
    private float text_size = 60;//���������ִ�С

    private float radius_center = 15;//���������ĵİ뾶���� radius_center

    private float width_hour = 20;//ʱ����
    private float width_minutes = 10;//����̶ȿ��
    private float width_second = 8;//����̶ȿ��


    private float density_second = 0.85f;//���볤�ȱ���
    private float density_minute = 0.70f;//���볤�ȱ���
    private float density_hour = 0.45f;//ʱ�볤�ȱ���


    private float mWidth = 1000;//����Ϊwrap_contentʱ��Ĭ�ϵĿ��
    private float mHeight = 1000;//����Ϊwrap_contentʱ��Ĭ�ϵĸ߶�

    private double millSecond, second, minute, hour;//��ȡ��ǰ��ʱ����������룬�룬���ӣ�Сʱ��

    public ClockView(Context context) {
        this(context, null, 0);
        Log.i("tag", "ClockView(Context context)");
    }

    public ClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        Log.i("tag", "ClockView(Context context, AttributeSet attrs)");
    }

    public ClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.clock);
        /**
         * �������е�R.styleable.OOXX,���е�OOXX�У�ǰ������ֱ���Ϊ��styleable��name����_��������
         * ������xml��ʹ�õ�ʱ�򣬲�����styleable�е�name����
         */
        width_circle = ta.getDimension(R.styleable.clock_width_circle, 5);//��������ԲȦ�Ŀ��
        width_longer = ta.getDimension(R.styleable.clock_width_longer, 5);//����̶ȿ��
        width_shorter = ta.getDimension(R.styleable.clock_width_shorter, 3);//������̶ȿ��
        length_longer = ta.getDimension(R.styleable.clock_length_longer, 60);//����̶ȳ���
        length_shorter = ta.getDimension(R.styleable.clock_length_shorter, 30);//������̶ȳ���
        text_size = ta.getDimension(R.styleable.clock_text_size, 60);//���������ִ�С
        radius_center = ta.getDimension(R.styleable.clock_radius_center, 15);//���������ĵİ뾶���� radius_center
        width_hour = ta.getDimension(R.styleable.clock_width_hour, 20);//ʱ����
        width_minutes = ta.getDimension(R.styleable.clock_density_minute, 10);//����̶ȿ��
        width_second = ta.getDimension(R.styleable.clock_density_second, 8);//����̶ȿ��
        density_second = ta.getFloat(R.styleable.clock_density_second, 0.85f);//���볤�ȱ���
        density_minute = ta.getFloat(R.styleable.clock_density_minute, 0.70f);//���볤�ȱ���
        density_hour = ta.getFloat(R.styleable.clock_density_hour, 0.45f);//ʱ�볤�ȱ���
        refresh_time = ta.getFloat(R.styleable.clock_refresh_time, 1000);
        ta.recycle();
        Log.i("tag", "ClockView(Context context, AttributeSet attrs, int defStyleAttr)");
    }


    private void init() {
        refreshThread = new Thread();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //�������Ӧwrap_content�Ľ������
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension((int) mWidth, (int) mHeight);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension((int) mWidth, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, (int) mHeight);
        }
        
        Log.i("tag", "onMeasure(int widthMeasureSpec, int heightMeasureSpec)");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // ��ȡ��߲���
        this.mWidth = Math.min(getWidth(), getHeight());
//        this.mWidth = getWidth();
//        this.mHeight = getHeight();
        this.mHeight = Math.max(getWidth(), getHeight());
        //��ȡʱ��
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(System.currentTimeMillis()));
        millSecond = calendar.get(Calendar.MILLISECOND);
        second = calendar.get(Calendar.SECOND);
        minute = calendar.get(Calendar.MINUTE);
        hour = calendar.get(Calendar.HOUR);

        // ����Բ
        Paint paintCircle = new Paint();
        paintCircle.setStyle(Paint.Style.STROKE);
        paintCircle.setAntiAlias(true);
        paintCircle.setStrokeWidth(width_circle);
        canvas.drawCircle(mWidth / 2,
                mHeight / 2, mWidth / 2 - width_circle, paintCircle);


        // ���̶�
        Paint painDegree = new Paint();
        painDegree.setAntiAlias(true);
        float lineLength = 0;
        for (int i = 0; i < 60; i++) {
            if (i % 5 == 0) {
                painDegree.setStrokeWidth(width_longer);
                lineLength = length_longer;
            } else {
                painDegree.setStrokeWidth(width_shorter);
                lineLength = length_shorter;
            }
            canvas.drawLine(mWidth / 2, mHeight / 2 - mWidth / 2 + width_circle, mWidth / 2, mHeight / 2 - mWidth / 2 + lineLength, painDegree);
            canvas.rotate(360 / 60, mWidth / 2, mHeight / 2);
        }

        painDegree.setTextSize(text_size);
        String targetText[] = getContext().getResources().getStringArray(R.array.clock);

        //����ʱ������
        float startX = mWidth / 2 - painDegree.measureText(targetText[1]) / 2;
        float startY = mHeight / 2 - mWidth / 2 + 120;
        float textR = (float) Math.sqrt(Math.pow(mWidth / 2 - startX, 2) + Math.pow(mHeight / 2 - startY, 2));

        for (int i = 0; i < 12; i++) {
            float x = (float) (startX + Math.sin(Math.PI / 6 * i) * textR);
            float y = (float) (startY + textR - Math.cos(Math.PI / 6 * i) * textR);
            if (i != 11 && i != 10 && i != 0) {
                y = y + painDegree.measureText(targetText[i]) / 2;
            } else {
                x = x - painDegree.measureText(targetText[i]) / 4;
                y = y + painDegree.measureText(targetText[i]) / 4;
            }
            canvas.drawText(targetText[i], x, y, painDegree);
        }


        //��������
        Paint paintSecond = new Paint();
        paintSecond.setAntiAlias(true);
        paintSecond.setStrokeWidth(width_second);
        paintSecond.setColor(Color.RED);
        drawSecond(canvas, paintSecond);

        //���Ʒ���
        Paint paintMinute = new Paint();
        paintMinute.setAntiAlias(true);
        paintMinute.setStrokeWidth(width_minutes);
        drawMinute(canvas, paintMinute);

        //����ʱ��
        Paint paintHour = new Paint();
        paintHour.setAntiAlias(true);
        paintHour.setStrokeWidth(width_hour);
        drawHour(canvas, paintHour);

        // ��Բ��
        Paint paintPointer = new Paint();
        paintPointer.setAntiAlias(true);
        paintPointer.setStyle(Paint.Style.FILL);
        canvas.drawCircle(mWidth / 2, mHeight / 2, radius_center, paintPointer);

        Log.i("tag", "onDraw(Canvas canvas)");
    }

    //��������
    private void drawSecond(Canvas canvas, Paint paint) {
//        float degree = (float) (second * 360 / 60 + millSecond / 1000 * 360 / 60);
        /*
         * ���������ĽǶȽ�����ϸ΢����
         * ���ˢ��ʱ��С��1�룬�����ǵĽǶȼ�������˺���
         * ���ˢ��ʱ�����1�룬��ȥ���˺�����нǶȼ���
         */
        float degree = refresh_time > 1000 ? (float) (second * 360 / 60) : (float) (second * 360 / 60 + millSecond / 1000 * 360 / 60);
        canvas.rotate(degree, mWidth / 2, mHeight / 2);
        canvas.drawLine(mWidth / 2, mHeight / 2, mWidth / 2, mHeight / 2 - (mWidth / 2 - width_circle) * density_second, paint);
        canvas.rotate(-degree, mWidth / 2, mHeight / 2);

        Log.i("tag", "drawSecond(Canvas canvas, Paint paint)");
    }

    //���Ʒ���
    private void drawMinute(Canvas canvas, Paint paint) {
        float degree = (float) (minute * 360 / 60);
        canvas.rotate(degree, mWidth / 2, mHeight / 2);
        canvas.drawLine(mWidth / 2, mHeight / 2, mWidth / 2, mHeight / 2 - (mWidth / 2 - width_circle) * density_minute, paint);
        canvas.rotate(-degree, mWidth / 2, mHeight / 2);
        Log.i("tag", "drawMinute(Canvas canvas, Paint paint)");
    }

    //����ʱ��
    private void drawHour(Canvas canvas, Paint paint) {
        float degreeHour = (float) hour * 360 / 12;
        float degreeMinut = (float) minute / 60 * 360 / 12;
        float degree = degreeHour + degreeMinut;
        canvas.rotate(degree, mWidth / 2, mHeight / 2);
        canvas.drawLine(mWidth / 2, mHeight / 2, mWidth / 2, mHeight / 2 - (mWidth / 2 - width_circle) * density_hour, paint);
        canvas.rotate(-degree, mWidth / 2, mHeight / 2);
        Log.i("tag", "drawHour(Canvas canvas, Paint paint)");
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //����ӵ�Activity��ʱ�������߳�
        refreshThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (BOOLEAN_REFRESH_TIME) {
                    //���ø��½����ˢ��ʱ��
                   SystemClock.sleep((long) refresh_time);
                    postInvalidate();//http://blog.csdn.net/mars2639/article/details/6650876
                    Log.i("tag", "refreshThread-->run()");
                }
            }
        });
        refreshThread.start();
        Log.i("tag", "onAttachedToWindow()");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //ֹͣˢ���߳�
        refreshThread.interrupt();
        Log.i("tag", "onDetachedFromWindow()");
        BOOLEAN_REFRESH_TIME = false;
    }
}