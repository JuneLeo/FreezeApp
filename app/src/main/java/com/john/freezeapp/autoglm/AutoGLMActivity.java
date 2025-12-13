package com.john.freezeapp.autoglm;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.john.freezeapp.R;
import com.john.freezeapp.ToolbarActivity;

public class AutoGLMActivity extends ToolbarActivity {
    @Override
    protected String getToolbarTitle() {
        return getString(R.string.app_auto_glm_name);
    }

    EditText etQuery;
    EditText etUri;
    EditText etModel;
    EditText etApiKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_glm);

        etUri = findViewById(R.id.et_url);
        etUri.setText(AutoGLMManager.getUrl().trim());

        etModel = findViewById(R.id.et_model);
        etModel.setText(AutoGLMManager.getModel().trim());

        etApiKey = findViewById(R.id.et_apikey);
        etApiKey.setText(AutoGLMManager.getApiKey().trim());

        etQuery = findViewById(R.id.et_query);

        AutoGLMService.start(getApplicationContext());

        findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = etQuery.getText().toString();
                if (TextUtils.isEmpty(text)) {
                    Toast.makeText(AutoGLMActivity.this, "query is empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                String url = etUri.getText().toString().trim();
                if (TextUtils.isEmpty(url)) {
                    Toast.makeText(AutoGLMActivity.this, "url is empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                String model = etModel.getText().toString().trim();
                if (TextUtils.isEmpty(model)) {
                    Toast.makeText(AutoGLMActivity.this, "model is empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                String apikey = etApiKey.getText().toString().trim();
                if (TextUtils.isEmpty(apikey)) {
                    Toast.makeText(AutoGLMActivity.this, "apikey is empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                saveAutoGLMConfig();
                AutoGLMManager.execute(text, url, model, apikey);
                etQuery.postDelayed(() -> refreshStatus(), 300);
            }
        });

        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AutoGLMManager.stopAutoGLM();
                refreshStatus();
            }
        });

        registerAutoGLMListener();
    }

    private void registerAutoGLMListener() {

    }


    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveAutoGLMConfig();
    }

    private void refreshStatus() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.tv_text)).setText("执行状态：" + (AutoGLMManager.isActive() ? "执行中" : "未执行"));
            }
        });
    }

    private void saveAutoGLMConfig() {
        AutoGLMManager.setUrl(etUri.getText().toString().trim());
        AutoGLMManager.setModel(etModel.getText().toString().trim());
        AutoGLMManager.setApiKey(etApiKey.getText().toString().trim());
    }


}
