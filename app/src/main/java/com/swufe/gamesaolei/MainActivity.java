package com.swufe.gamesaolei;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class MainActivity extends AppCompatActivity {
    private PuzzleView puzzleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        puzzleView = new PuzzleView(this, null);
        setContentView(puzzleView);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img); // 替换为你的图片
        puzzleView.initializePuzzle(bitmap);
    }
}