package com.example.uberv.fkkenubercalculator;

import android.content.res.Resources;
import android.graphics.Color;
import android.icu.math.BigDecimal;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    public static final char[] OPERATIONS = {'+', '-', '/', '÷', '×', '*'};
    public static final String OPERATIONS_REGEX = "[+\\-/*]";
    private static final String TAG_LICENSE_DIALOG = "TAG_LICENSE_DIALOG";

    // VIEWS
    @BindView(R.id.input_tv)
    TextView mInputTv;
    @BindView(R.id.output_tv)
    TextView mOutputTv;
    @BindView(R.id.op_button_del)
    Button mDeleteBtn;
    @BindView(R.id.input_scrollview)
    HorizontalScrollView mInputHorizontalSv;
    @BindView(R.id.root_layout)
    LinearLayout rootLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    private float defaultFontSize;
    private float fontSize;
    private float minFontSize;
    private int lastWidth = 0;
    // MEMBERS VARIABLES
    private StringBuilder mEquationBuilder = new StringBuilder();
    private Evaluator mEvaluator;
    private char lastChar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("151RDB492");

        Toast.makeText(this, R.string.claimer, Toast.LENGTH_LONG).show();

        mEvaluator = new Evaluator();

        // configure text sizes (SP)
        fontSize = mInputTv.getTextSize() / getResources().getDisplayMetrics().scaledDensity;
        defaultFontSize = fontSize;
        minFontSize = 24;

        // butterknife does not support long clicks
        mDeleteBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                delete();
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.license:
                showLicense();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLicense() {
        MessageDialogFragment licenseDialog = MessageDialogFragment.newInstance(getResources().getString(R.string.license));
        licenseDialog.show(getSupportFragmentManager(), TAG_LICENSE_DIALOG);
    }

    @OnClick({R.id.digit_btn_0, R.id.digit_btn_1, R.id.digit_btn_2,
            R.id.digit_btn_3, R.id.digit_btn_4, R.id.digit_btn_5,
            R.id.digit_btn_6, R.id.digit_btn_7, R.id.digit_btn_8,
            R.id.digit_btn_9, R.id.digit_btn_point,
            R.id.op_button_add, R.id.op_button_sub,
            R.id.op_button_div, R.id.op_button_mult})
    void OnDigitInput(Button button) {
        // user pressed a dot (.)
        if (button.getId() == R.id.digit_btn_point) {
            // check whether we can place a dot
            if (!canPlaceDot()) return;
        }
        // handle last "=" press
        if (mDeleteBtn.getText().equals("CLR")) {
            // last pressed button is "="
            if (isOperation(button.getText().toString().charAt(0))) {
                if (mInputTv.getText().toString().equals("Infinity")) {
                    delete();
                }
                // else do nothing
            } else {
                // user pressed new digit
                delete();
            }
            mDeleteBtn.setText("DEL");
        }
        char newChar = button.getText().toString().charAt(0);
        // ignore first operation character if it is not -
        if (mEquationBuilder.length() == 0 && isOperation(newChar) && newChar != '-') {
            return;
        }
        // replace previous operation
        if (isOperation(newChar)) {
            // if last char is dot, we only expect digits as input
            if (lastChar == '.') return;
            // if last char is operation and new char is operation - delete last operation,
            // so it will get replace withe new one
            if (isOperation(lastChar)) {
                mEquationBuilder.deleteCharAt(mEquationBuilder.length() - 1);
            }
        }
        // add new char
        mEquationBuilder.append(newChar);
        int outputWidth = mInputTv.getWidth();
        int maxWidth = rootLayout.getWidth();
        // TODO REFACTOR ^^^
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());
        if (outputWidth + px > maxWidth && fontSize > minFontSize) {
            fontSize = fontSize * ((float) maxWidth / (outputWidth + px));
            if (fontSize < minFontSize) {
                Log.d(LOG_TAG,"font is min size");
                fontSize = minFontSize;
            }
            mInputTv.setTextSize(fontSize);
        } else {
            lastWidth = outputWidth;
        }

        lastChar = newChar;
        mInputTv.setText(mEquationBuilder.toString());
        String answer = calculate();
        if (answer != null) {
            mOutputTv.setText(answer);
        }
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
        String tmpEquation = toJEvalEquation(mEquationBuilder.toString());
        String[] numbers = tmpEquation.split(OPERATIONS_REGEX);
        String lastNumber = numbers[numbers.length - 1];
        return !lastNumber.contains(".") || isOperation(tmpEquation.charAt(tmpEquation.length() - 1));
    }

    /**
     * convert user-friendly equation to JEval format (replace ÷ with / and etc)
     *
     * @return
     */
    private String toJEvalEquation(String equation) {
        return equation.replace('÷', '/').replace('×', '*');
    }

    private void resetFontSize() {
        mInputTv.setTextSize(defaultFontSize);
        fontSize = defaultFontSize;
    }

    private String calculate() {
        // operate on mEquation
        String output = null;
        if (mEquationBuilder.length() > 0) {
            // remove last operation symbol if there are no digits after it
            String tmpEquation = isOperation(mEquationBuilder.charAt(mEquationBuilder.length() - 1)) ? // is last char an operation?
                    mEquationBuilder.toString().substring(0, mEquationBuilder.length() - 1) :   // yes - remove it
                    mEquationBuilder.toString();                                                // no - okay
            // make equation JEval-friendly
            tmpEquation = toJEvalEquation(tmpEquation);
            try {
                output = mEvaluator.evaluate(tmpEquation);
                Log.d(LOG_TAG, "calculating: " + tmpEquation + " = " + output);
            } catch (EvaluationException e) {
                Log.w(LOG_TAG, "failed to calculate " + tmpEquation);
            }
            // no exception occurred => calculated successfully
            return output;
        } else {
            // nothing to calculate (reset text in case of backspace function)
            mOutputTv.setText("");
            return null;
        }
    }


    void delete() {
        mEquationBuilder = new StringBuilder();
        mOutputTv.setText("");
        mInputTv.setText("");
        resetFontSize();
    }

    @OnClick(R.id.op_button_del)
    void backspace() {
        if (mDeleteBtn.getText().equals("CLR")) {
            delete();
            mDeleteBtn.setText("DEL");
            return;
        }
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
            String answer = calculate();
            if (answer != null) {
                mOutputTv.setText(answer);
            }

            // check if we can enlarge the text size
            // TODO REFACTOR
            int outputWidth = mInputTv.getWidth();
            int maxWidth = rootLayout.getWidth();
            Resources r = getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());
            if (fontSize < defaultFontSize && outputWidth+px<maxWidth) {
                // enlarge font size
                fontSize = fontSize * ((float) maxWidth / (outputWidth));
                // ...
                if (fontSize > defaultFontSize) {
                    Log.d(LOG_TAG,"font is max size");
                    fontSize = defaultFontSize;
                }
                mInputTv.setTextSize(fontSize);
            }
        }
    }

    @OnClick(R.id.op_button_equals)
    void equals() {
        String answer = calculate();
        if (!isEmptyOrNull(answer)) {
            mInputTv.setText(answer);
            mEquationBuilder = new StringBuilder(answer);
            mOutputTv.setText("");
            mDeleteBtn.setText("CLR");
        }
        // else - ignore

    }

    private boolean isEmptyOrNull(String string) {
        return string == null || string.isEmpty();
    }

    private boolean isOperation(char c) {
        for (char operation : OPERATIONS) {
            if (operation == c) return true;
        }
        return false;
    }
}
