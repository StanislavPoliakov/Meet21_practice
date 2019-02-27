package home.stanislavpoliakov.meet21_practice;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MyCustomView extends View {
    private static final String TAG = "meet21_logs";
    private int sp = 0;
    private float centerX;
    private float centerY;
    private int arcColor;
    private int arrowColor;
    private int changeSpeedTo;
    private float textSize;
    private int textColor;

    public MyCustomView(Context context) {
        super(context);
        init();
    }

    public MyCustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyCustomView);
        arcColor = typedArray.getColor(R.styleable.MyCustomView_arc_color, Color.GRAY);
        arrowColor = typedArray.getColor(R.styleable.MyCustomView_arrow_color, Color.GRAY);
        textSize = typedArray.getFloat(R.styleable.MyCustomView_text_size, 24);
        textColor = typedArray.getColor(R.styleable.MyCustomView_text_color, Color.GRAY);
        typedArray.recycle();
        init();
    }

    public MyCustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public MyCustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {

    }

    /**
     * Отрисовываем наши компоненты
     * @param canvas холст
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawArcs(canvas);
        drawSpeedValue(canvas, sp);
        drawArrow(canvas, sp);

    }

    /**
     * Рисуем окружности (арки) для спидометра
     * @param canvas холст
     */
    private void drawArcs(Canvas canvas) {

        //Получаем границы прямоугольника арки
        float left = getPaddingLeft() + 20;
        float top = getPaddingTop() + 100;
        float right = getWidth() - getPaddingRight() - 20;
        float bottom = top + right;

        RectF arcRect = new RectF(left, top, right, bottom);

        //Делаем изображение плавным
        Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        //Рисуем без заливки
        arcPaint.setStyle(Paint.Style.STROKE);

        arcPaint.setColor(arcColor);

        Path arcsPath = new Path();

        //Стартовый угол -210, длина = 240 угловых единиц
        arcsPath.addArc(arcRect, -210, 240);

        // Меняем параметры для второй арки
        arcRect.left += 50;
        arcRect.right -= 50;
        arcRect.top += 50;
        arcRect.bottom -= 50;
        arcsPath.addArc(arcRect, -210, 240);

        canvas.drawPath(arcsPath, arcPaint);
    }

    /**
     * Рисуем стрелку
     * @param canvas холст
     * @param reqAngle в нашем случае скорость равна угловым единицам
     */
    private void drawArrow(Canvas canvas, int reqAngle) {
        Path arrowPath = new Path();
        Paint arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arrowPaint.setColor(arrowColor);
        arrowPaint.setStyle(Paint.Style.STROKE);

        //Вертикальное положение стрелки - 120 градусов
        // В этом положении мы отрисуем стрелку, а потом просто будем ее поворачивать
        int speedPoint = 120;

        // Угол поворота, напомню, что окружность - это 2 * Pi
        double arrowAngle = Math.PI / (180f / (30 - speedPoint)) ;


        // Центр фигуры и стрелки
        centerX = getWidth() / 2f;
        centerY = (getWidth() - getPaddingRight() - getPaddingLeft()) / 2f;
        centerY += 100;

        //Внутренний радиус стрелки
        float innerRadius = 150;

        //Высота стрелки
        float arrowHeight = centerY - 100 - getPaddingTop();

        //Вертикальный катет для точки отклонения
        // Это точка касательной к окружности. Для высоты стрелки (H) - это R * R / H
        double a = Math.pow(innerRadius, 2) / arrowHeight;

        //Горизонатльный катет для точки отклонения
        //Вычисляем по теореме Пифагора, так как предыдущий катет мы вычислили
        double b = Math.sqrt(Math.pow(innerRadius, 2) - Math.pow(a, 2));

        //Координаты касательной (от точки соприкосновения) слева...
        float startX = (float) (centerX - b);
        float startY = (float) (centerY - a);

        // И справа... endY = startY
        float endX = (float) (centerX + b);

        // Вычисляем конец стрелки.

        // Определяем радиус (для вычислений)
        float radius = centerX - getPaddingLeft() - 20;

        arrowPath.moveTo(startX, startY);

        // координаты конца стрелки
        float arrowPositionX = (float) (centerX - radius * Math.cos(arrowAngle));
        float arrowPositionY = (float) (centerY + radius * Math.sin(arrowAngle));

        //Рисуем линии
        arrowPath.lineTo(arrowPositionX, arrowPositionY);
        arrowPath.moveTo(endX, startY);
        arrowPath.lineTo(arrowPositionX, arrowPositionY);

        //И внутреннюю окружность
        RectF arrowRect = new RectF(centerX - innerRadius, centerY - innerRadius,
                centerX + innerRadius, centerY + innerRadius);

        arrowPath.addArc(arrowRect, -210 - 90 +  speedPoint , 180);

        //Поворачиваем на заданный угол
        canvas.rotate(reqAngle - speedPoint, centerX, centerY);
        canvas.drawPath(arrowPath, arrowPaint);

    }

    /**
     * Рисуем буквенное значение скорости
     * @param canvas холст
     * @param speedPoint значение скорости
     */
    private void drawSpeedValue(Canvas canvas, int speedPoint) {
        Paint textPaint = new Paint();
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);

        //1px = 0.75pt, следовательно, 72pt = 96px, 96/2 = 48. Это чтобы приблизительно по центру
        canvas.drawText(String.valueOf(speedPoint), centerX - textSize / 1.5f, centerY, textPaint);
    }

    /**
     * При нажатии на view определяем координаты и меняем положение стрелки
     * @param event что случилось
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            //Получаем величину отклонения точки от центра стрелки
            float calcWidth = centerX - event.getX();
            float calcHeight = centerY - event.getY();

            // Если клик произошел выше центра стрелки
            if (calcHeight > 0) {

                //Преобразуем обратно в угловые величины
                changeSpeedTo = (int) (Math.atan(calcWidth / calcHeight) * -180 / Math.PI) + 120;

                //Если ниже центра и левее - то меняем арктангенс (на арккотангенс) и даем смещение (-90)
            } else if (calcWidth >= 0) {

                // Значение принять не ниже 0
                changeSpeedTo = Math.max(0, (int) (Math.atan(calcHeight / calcWidth) * 180 / Math.PI) + 30);

                //Если ниже центра и левее - то меняем арктангенс (на арккотангенс) и даем смещение (+90)
            } else {

                //Значение принять не больше 240
                changeSpeedTo = Math.min((int) (Math.atan(calcHeight / calcWidth) * 180 / Math.PI) + 210, 240);
            }

            //Log.d(TAG, "onTouchEvent: changeSpeedTo = " + changeSpeedTo);
        }


        /**
         * Наша анимация в отдельном потоке. Создаем Runnable, а затем создаем на него поток и запускаем
         */
        Runnable changeSpeed = () -> {

            //Если ткнули левее стрелки
            if (sp < changeSpeedTo) {
                while (sp < changeSpeedTo - 1) {
                    SystemClock.sleep(10);
                    sp++;
                    postInvalidate();
                }

                //И если правее
            } else {
                while (sp > changeSpeedTo + 1) {
                    SystemClock.sleep(10);
                    sp--;
                    postInvalidate();
                }
            }
        };

        Thread t = new Thread(changeSpeed);
        t.start();

        return true;
    }
}
