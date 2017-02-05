package com.example.uberv.fkkenubercalculator;

import android.icu.math.BigDecimal;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.HorizontalScrollView;
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
    // CONSTANTS
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static final char NULL_CHAR = '\u0000';

    // VIEWS
    @BindView(R.id.input_tv)
    TextView mInputTv;
    @BindView(R.id.output_tv)
    TextView mOutputTv;
    @BindView(R.id.op_button_del)
    Button mDeleteBtn;
    @BindView(R.id.input_scrollview)
    HorizontalScrollView mInputHorizontalSv;
    // MEMBERS VARIABLES
    private StringBuilder mEquationBuilder = new StringBuilder();
    private Evaluator mEvaluator;
    private char lastChar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mEvaluator = new Evaluator();
        // butterknife does not support long clicks
        mDeleteBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                delete();
                return true;
            }
        });
    }

    @OnClick({R.id.digit_btn_0, R.id.digit_btn_1, R.id.digit_btn_2,
            R.id.digit_btn_3, R.id.digit_btn_4, R.id.digit_btn_5,
            R.id.digit_btn_6, R.id.digit_btn_7, R.id.digit_btn_8,
            R.id.digit_btn_9, R.id.digit_btn_point,
            R.id.op_button_add, R.id.op_button_sub,
            R.id.op_button_div})
    void OnDigitInput(Button button) {
        // TODO disable first operation character if it is not -
        char newChar = button.getText().toString().charAt(0);
        // if last char was operation, and new char is operation, then replace last operation
        if (isOperation(lastChar) && isOperation(newChar)) {
            mEquationBuilder.deleteCharAt(mEquationBuilder.length() - 1).append(newChar);
        } else {
            mEquationBuilder.append(newChar);
        }
        lastChar = newChar;
        mInputTv.setText(mEquationBuilder.toString());
        calculate();
        scrollToRight();
    }

    private void scrollToRight() {
        // fix a bug where HorizontalScrollView is not scroll fully to the right
        ViewTreeObserver vto = mInputHorizontalSv.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // remove this listener
                mInputHorizontalSv.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                mInputHorizontalSv.scrollTo(mInputTv.getWidth(), 0);
            }
        });
        // TODO do the same for output scroll view
    }

    /**
     * check if last input number doesn't have a dot (.), so we can place one
     */
    private boolean canPlaceDot() {

        return false;
    }

    private void calculate() {
        // operate on mEquation
        String output = new String();
        if (mEquationBuilder.length() > 0) {
            String tmpEquation = isOperation(mEquationBuilder.charAt(mEquationBuilder.length() - 1)) ?
                    mEquationBuilder.toString().substring(0, mEquationBuilder.length() - 1) :
                    mEquationBuilder.toString();
            tmpEquation = tmpEquation.replace('÷', '/');
            try {
                output = mEvaluator.evaluate(tmpEquation);
                Log.d(LOG_TAG, "calculating: " + tmpEquation + " = " + output);
            } catch (EvaluationException e) {
                Log.w(LOG_TAG, "failed to calculate " + tmpEquation);
//            e.printStackTrace();
            }
            if (!isEmptyOrNull(output)) {
                // no exception occurred => calculated successfully
                mOutputTv.setText(output);
            }
        } else {
            // nothing to calculate (reset text in case of backspace function)
            mOutputTv.setText("");
        }
    }


    void delete() {
        mEquationBuilder = new StringBuilder();
        mOutputTv.setText("");
        mInputTv.setText("");
    }

    @OnClick(R.id.op_button_del)
    void backspace() {
        if (mEquationBuilder.length() > 0) {
            // backspace
            mEquationBuilder.deleteCharAt(mEquationBuilder.length() - 1);
            // check if we have characters left
            if (mEquationBuilder.length() > 0) {
                // since we delete last character, set lastChar to new last character
                lastChar = mEquationBuilder.charAt(mEquationBuilder.length() - 1);
            } else {
                // nothing left...
                lastChar = NULL_CHAR;
            }
            // since we delete last character, set lastChar to new last character
            mInputTv.setText(mEquationBuilder.toString());
            // recalculate answer
            calculate();
        }
    }

    @OnClick(R.id.op_button_equals)
    void equals() {

    }

    private boolean isEmptyOrNull(String string) {
        return string == null || string.isEmpty();
    }

    private boolean isOperation(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '÷';
    }
}
