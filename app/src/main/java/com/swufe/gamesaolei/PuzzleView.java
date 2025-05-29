package com.swufe.gamesaolei;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PuzzleView extends View {
    private List<PuzzlePiece> pieces;
    private int numColumns = 3;
    private int pieceSize;
    private int scaledPieceSize;
    private Paint paint;
    private Paint textPaint;
    private Integer firstSelectedIndex = null;
    private float scaleFactor = 1.5f;
    private int swapCount = 0;
    private long startTime ;
    private Handler handler = new Handler();
    private boolean isRunning = false;

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                long elapsedTime = (System.currentTimeMillis() - startTime) / 1000; // 计算经过的时间（秒）
                if (elapsedTime >= 120) {
                    endGame("时间到，游戏结束!");
                    return;
                }
                invalidate();
                handler.postDelayed(this, 1000);
            }
        }
    };

    public PuzzleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        textPaint = new Paint();
        textPaint.setColor(0xFF000000);
        textPaint.setTextSize(50);
        pieces = new ArrayList<>();
    }

    public void initializePuzzle(Bitmap bitmap) {
        pieceSize = bitmap.getWidth() / numColumns;
        scaledPieceSize = (int) (pieceSize * scaleFactor);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img, options);

        pieceSize = bitmap.getWidth() / numColumns;
        scaledPieceSize = (int) (pieceSize * scaleFactor);


        for (int i = 0; i < numColumns; i++) {
            for (int j = 0; j < numColumns; j++) {

                Bitmap pieceBitmap = Bitmap.createBitmap(bitmap, j * pieceSize, i * pieceSize, pieceSize, pieceSize);
                pieceBitmap = Bitmap.createScaledBitmap(pieceBitmap, scaledPieceSize, scaledPieceSize, true);
                pieces.add(new PuzzlePiece(pieceBitmap, i * numColumns + j));
            }
        }
        shufflePuzzle();
        invalidate();
    }

    private void shufflePuzzle() {
        Collections.shuffle(pieces);
    }

    private void startTimer() {
        startTime = System.currentTimeMillis();
        isRunning = true;
        handler.postDelayed(timerRunnable, 1000);
    }

    private void endGame(String message) {
        isRunning = false;
        handler.removeCallbacks(timerRunnable);

        long totalTime = (isRunning ? (System.currentTimeMillis() - startTime) / 1000 : 0);
        Toast.makeText(getContext(), message + " 用时: " + totalTime + "秒", Toast.LENGTH_SHORT).show(); // 显示结束提示
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int totalWidth = scaledPieceSize * numColumns;
        int totalHeight = scaledPieceSize * numColumns;
        int startX = (getWidth() - totalWidth) / 2;
        int startY = (getHeight() - totalHeight) / 2;

        for (int i = 0; i < pieces.size(); i++) {
            PuzzlePiece piece = pieces.get(i);
            int x = startX + (i % numColumns) * scaledPieceSize;
            int y = startY + (i / numColumns) * scaledPieceSize;
            canvas.drawBitmap(piece.getBitmap(), x, y, paint);
        }

        canvas.drawText("交换次数: " + swapCount, startX, startY - 20, textPaint);

        long elapsedTime = (isRunning ? (System.currentTimeMillis() - startTime) / 1000 : 0);

        String timeFormatted = String.format("%02d:%02d", elapsedTime / 60, elapsedTime % 60);
        canvas.drawText("时间: " + timeFormatted, startX, startY - 70, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int startX = (getWidth() - (scaledPieceSize * numColumns)) / 2;
            int startY = (getHeight() - (scaledPieceSize * numColumns)) / 2;
            int x = (int) (event.getX() - startX) / scaledPieceSize;
            int y = (int) (event.getY() - startY) / scaledPieceSize;
            int clickedIndex = y * numColumns + x;

            if (clickedIndex >= 0 && clickedIndex < pieces.size()) {
                if (!isRunning) {
                    startTimer();
                }
                handleSelection(clickedIndex);
            }
            invalidate();
        }
        return true;
    }

    private void handleSelection(int clickedIndex) {
        if (firstSelectedIndex == null) {
            firstSelectedIndex = clickedIndex;
        } else {
            if (isAdjacent(firstSelectedIndex, clickedIndex)) {
                swapPieces(firstSelectedIndex, clickedIndex);
                swapCount++;
            }
            firstSelectedIndex = null;
        }
    }

    private void swapPieces(int index1, int index2) {
        Collections.swap(pieces, index1, index2);
        checkCompletion();
    }

    private boolean isAdjacent(int index1, int index2) {
        int row1 = index1 / numColumns;
        int col1 = index1 % numColumns;
        int row2 = index2 / numColumns;
        int col2 = index2 % numColumns;

        return (Math.abs(row1 - row2) + Math.abs(col1 - col2) == 1);
    }


    private void checkCompletion() {
        boolean completed = true;
        for (int i = 0; i < pieces.size(); i++) {
            if (pieces.get(i).getPosition() != i) {
                completed = false;
                break;
            }
        }
        if (completed) {
            long totalTime = (System.currentTimeMillis() - startTime) / 1000;
            endGame("拼图完成! 总交换次数: " + swapCount + ", 用时: " + totalTime + "秒");
        }
    }
}

