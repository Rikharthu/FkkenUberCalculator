package com.example.uberv.fkkenubercalculator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;


// TODO Create custom button style and output horizontal scroll
// TODO implement dot check
// TODO custom animation

public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.input_tv)
    TextView mInputTv;
    @BindView(R.id.output_tv)
    TextView mOutputTv;
    private String mEquation = "";
    private Evaluator mEvaluator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mEvaluator = new Evaluator();
    }

    @OnClick({R.id.digit_btn_0, R.id.digit_btn_1, R.id.digit_btn_2,
            R.id.digit_btn_3, R.id.digit_btn_4, R.id.digit_btn_5,
            R.id.digit_btn_6, R.id.digit_btn_7, R.id.digit_btn_8,
            R.id.digit_btn_9, R.id.digit_btn_point,
            R.id.op_button_add, R.id.op_button_sub,
            R.id.op_button_div})
    void OnDigitInput(Button button) {
        mEquation = mInputTv.getText().toString() + button.getText();
        mInputTv.setText(mEquation);
        mEquation = mEquation.replace('÷', '/');
        calculate();
    }

    private void calculate() {
        // operate on mEquation
        String output = new String();
        try {
            output = mEvaluator.evaluate(mEquation);
            Log.d(LOG_TAG, "calculating: " + mEquation + " = " + output);
        } catch (EvaluationException e) {
            Log.w(LOG_TAG, "failed to calculate " + mEquation);
//            e.printStackTrace();
        }
        if(!isEmptyOrNull(output)){
            mOutputTv.setText(output);
        }
    }

    @OnClick(R.id.op_button_del)
    void delete() {
        mEquation = "";
        mOutputTv.setText("");
        mInputTv.setText("");
    }

    @OnClick(R.id.op_button_equals)
    void equals(){

    }

    private boolean isEmptyOrNull(String string) {
        return string == null || string.isEmpty();
    }
}
