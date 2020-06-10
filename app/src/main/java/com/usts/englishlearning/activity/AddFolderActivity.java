package com.usts.englishlearning.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.usts.englishlearning.R;
import com.usts.englishlearning.database.WordFolder;
import com.usts.englishlearning.util.TimeController;

public class AddFolderActivity extends BaseActivity {

    private EditText editName, editRemark;

    private RelativeLayout layoutAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_folder);

        init();

        layoutAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(editName.getText().toString().trim())) {
                    WordFolder wordFolder = new WordFolder();
                    wordFolder.setCreateTime(TimeController.getCurrentTimeStamp() + "");
                    wordFolder.setName(editName.getText().toString().trim());
                    if (!TextUtils.isEmpty(editRemark.getText().toString().trim()))
                        wordFolder.setRemark(editRemark.getText().toString().trim());
                    wordFolder.save();
                    onBackPressed();
                    Toast.makeText(AddFolderActivity.this, "新建成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddFolderActivity.this, "请输入完整", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void init() {
        editName = findViewById(R.id.edit_af_name);
        editRemark = findViewById(R.id.edit_af_remark);
        layoutAdd = findViewById(R.id.layout_af_add);
    }

}
