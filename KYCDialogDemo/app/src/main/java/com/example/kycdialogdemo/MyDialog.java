package com.example.kycdialogdemo;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by yaochao on 2019/02/27
 */
public class MyDialog extends Dialog {
	//    style引用style样式
	public MyDialog(Context context, int width, int height, View layout, int style) {

		super(context, style);

		setContentView(layout);

		Window window = getWindow();

		WindowManager.LayoutParams params = window.getAttributes();

		params.gravity = Gravity.CENTER;

		window.setAttributes(params);
	}
}
