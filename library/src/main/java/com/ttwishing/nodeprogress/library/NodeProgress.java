package com.ttwishing.nodeprogress.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kurt on 6/17/15.
 * 节点数
 */
public class NodeProgress extends View {

    private int nodeRadius = 20;  //节点的半径

    private Paint linePaint;
    private Paint nodePaint;
    private TextPaint textPaint;

    private int textColor = Color.parseColor("#dbdbdb"), textColorProgressed = Color.parseColor("#bb2212");
    private int lineColor = Color.parseColor("#dbdbdb"), lineColorProgressed = Color.parseColor("#bb2212");

    private int position = 0;

    private List<Node> nodes = new ArrayList<>();
    private CharSequence[] titles;

    private Drawable doneDrawable, undoDrawable, lastDrawable;


    public NodeProgress(Context context) {
        this(context, null);
    }

    public NodeProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NodeProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.NodeProgress);
            this.titles = a.getTextArray(R.styleable.NodeProgress_node_values);
            this.position = a.getIndex(R.styleable.NodeProgress_node_index);
            Log.d("kurt_test", ""+this.position+", "+this.titles.length);
            a.recycle();
        }

        this.doneDrawable = getResources().getDrawable(R.drawable.nodeprogress_icon_done);
        this.undoDrawable = getResources().getDrawable(R.drawable.nodeprogress_icon_undo);
        this.lastDrawable = getResources().getDrawable(R.drawable.nodeprogress_icon_complete);

        this.nodeRadius = this.undoDrawable.getIntrinsicWidth() / 2;

        this.nodePaint = new Paint();
        this.nodePaint.setAntiAlias(true);

        this.linePaint = new Paint();
        this.linePaint.setAntiAlias(true);
        this.linePaint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.nodeprogress_line_height_default));

        this.textPaint = new TextPaint();
        this.textPaint.setAntiAlias(true);
        this.textPaint.setColor(Color.GRAY);
        this.textPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.nodeprogress_text_size_default));
    }

    public void setProgress(int position) {
        if (this.position == position)
            return;
        this.position = position;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (titles == null || titles.length < 2 || position > titles.length - 1) {
            return;
        }

        int interval = (getMeasuredWidth() - 2 * nodeRadius) / (titles.length + 1);

        //组装nodes
        nodes = new ArrayList<>();
        int i = 0;
        for (CharSequence title : titles) {
            Point point = new Point(nodeRadius + interval * (i + 1), nodeRadius);
            nodes.add(new Node(point, title));
            i++;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (nodes == null || nodes.size() < 2 || position > nodes.size() - 1) {
            return;
        }

        //头节点
        Node first_node = nodes.get(0);

        //绘制完成的line
        Node current = nodes.get(position);
        linePaint.setColor(lineColorProgressed);
        canvas.drawLine(first_node.point.x, nodeRadius, current.point.x, nodeRadius, linePaint);

        //绘制未完成的line
        Node last_node = nodes.get(nodes.size() - 1);
        linePaint.setColor(lineColor);
        canvas.drawLine(current.point.x, nodeRadius, last_node.point.x, nodeRadius, linePaint);

        //遍历绘制各节点
        int i = 0;
        for (Node node : nodes) {
            Point point = node.point;
            Drawable drawable;
            if (i <= position) {
                drawable = doneDrawable;
            } else {
                if (i == nodes.size() - 1) {
                    drawable = lastDrawable;
                } else {
                    drawable = undoDrawable;
                }
            }
            drawable.setBounds(point.x - nodeRadius, point.y - nodeRadius, point.x + nodeRadius, point.y + nodeRadius);
            drawable.draw(canvas);
            canvas.save();

            if (i <= position) {
                textPaint.setColor(textColorProgressed);
            } else {
                textPaint.setColor(textColor);
            }

            //绘制文字
            float textWidth = textPaint.measureText(node.title, 0, node.title.length());//v0
            StaticLayout layout = new StaticLayout(node.title, textPaint, (int) textWidth, Layout.Alignment.ALIGN_CENTER, 1.0F, 0.0F, true);
            canvas.translate(point.x - layout.getWidth() / 2, point.y + 2 * nodeRadius);
            layout.draw(canvas);
            canvas.restore();
            i++;
        }
    }

    public class Node {
        public Point point;
        public CharSequence title;

        public Node(Point point, CharSequence title) {
            this.point = point;
            this.title = title;
        }
    }
}
