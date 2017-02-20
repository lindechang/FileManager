package lindec.learning.plan.filemanager;


import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;


public class fileActivity extends ListActivity {
    private static final String ROOT_PATH = "/";
    //存储文件名称
    private ArrayList<String> mFileName = null;
    //存储文件路径
    private ArrayList<String> mFilePath = null;
    //重命名布局xml文件显示dialog
    private View view;

    private EditText editText;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.files_activity);
        //显示文件列表
        showFileDir(ROOT_PATH);
    }

    /**
     * 扫描显示文件列表
     *
     * @param path
     */
    private void showFileDir(String path) {
        mFileName = new ArrayList<String>();
        mFilePath = new ArrayList<String>();
        File file = new File(path);

        File[] files = file.listFiles();
        //如果当前目录不是根目录
        if (!ROOT_PATH.equals(path)) {
            mFileName.add("@1");
            mFilePath.add(ROOT_PATH);
            mFileName.add("@2");
            mFilePath.add(file.getParent());
        }
        //添加所有文件
        for (File f : files) {
            mFileName.add(f.getName());
            mFilePath.add(f.getPath());
        }
        this.setListAdapter(new FileAdapter(this, mFileName, mFilePath));
    }

    /**
     * 点击事件
     *
     * @param l
     * @param v
     * @param position
     * @param id
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String path = mFilePath.get(position);
        File file = new File(path);
        // 文件存在并可读
        if (file.exists() && file.canRead()) {
            if (file.isDirectory()) {
                //显示子目录及文件
                showFileDir(path);
            } else {
                //处理文件
                fileHandle(file);
            }
        }
        //没有权限
        else {
            Resources res = getResources();
            new AlertDialog.Builder(this).setTitle("Message")
                    .setMessage(res.getString(R.string.no_permission))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(), "没有权限删除不了..", Toast.LENGTH_SHORT).show();
                        }
                    }).show();
        }
        super.onListItemClick(l, v, position, id);
    }

    //对文件进行增删改
    private void fileHandle(final File file) {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 打开文件
                if (which == 0) {
                    openFile(file);
                }
                //修改文件名
                else if (which == 1) {
                    LayoutInflater factory = LayoutInflater.from(fileActivity.this);
                    view = factory.inflate(R.layout.rename_dialog, null);
                    editText = (EditText) view.findViewById(R.id.editText);
                    editText.setText(file.getName());

                    DialogInterface.OnClickListener listener2 = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            String modifyName = editText.getText().toString();
                            final String fpath = file.getParentFile().getPath();
                            final File newFile = new File(fpath + "/" + modifyName);
                            if (newFile.exists()) {
                                //排除没有修改情况
                                if (!modifyName.equals(file.getName())) {
                                    new AlertDialog.Builder(fileActivity.this)
                                            .setTitle("注意!")
                                            .setMessage("文件名已存在，是否覆盖？")
                                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (file.renameTo(newFile)) {
                                                        showFileDir(fpath);
                                                        displayToast("重命名成功！");
                                                    } else {
                                                        displayToast("重命名失败！");
                                                    }
                                                }
                                            })
                                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            })
                                            .show();
                                }
                            } else {
                                if (file.renameTo(newFile)) {
                                    showFileDir(fpath);
                                    displayToast("重命名成功！");
                                } else {
                                    displayToast("重命名失败！");
                                }
                            }
                        }
                    };
                    AlertDialog renameDialog = new AlertDialog.Builder(fileActivity.this).create();
                    renameDialog.setView(view);
                    renameDialog.setButton("确定", listener2);
                    renameDialog.setButton2("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                        }
                    });
                    renameDialog.show();
                }
                //删除文件
                else {
                    new AlertDialog.Builder(fileActivity.this)
                            .setTitle("注意!")
                            .setMessage("确定要删除此文件吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (file.delete()) {
                                        //更新文件列表
                                        showFileDir(file.getParent());
                                        displayToast("删除成功！");
                                    } else {
                                        displayToast("删除失败！");
                                    }
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                }
            }
        };
        //选择文件时，弹出增删该操作选项对话框
        String[] menu = {"打开文件", "重命名", "删除文件"};
        new AlertDialog.Builder(fileActivity.this)
                .setTitle("请选择要进行的操作!")
                .setItems(menu, listener)
                .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }

    //打开文件
    private void openFile(File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        String type = getMIMEType(file);
        intent.setDataAndType(Uri.fromFile(file), type);
        startActivity(intent);
    }

    //获取文件mimetype
    private String getMIMEType(File file) {
        String type = "";
        String name = file.getName();
        //文件扩展名
        String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
        if (end.equals("m4a") || end.equals("mp3") || end.equals("wav")) {
            type = "audio";
        } else if (end.equals("mp4") || end.equals("3gp")) {
            type = "video";
        } else if (end.equals("jpg") || end.equals("png") || end.equals("jpeg") || end.equals("bmp") || end.equals("gif")) {
            type = "image";
        } else {
            //如果无法直接打开，跳出列表由用户选择
            type = "*";
        }
        type += "/*";
        return type;
    }

    private void displayToast(String message) {
        Toast.makeText(fileActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        //super.getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.add(Menu.NONE, Menu.FIRST + 1, 7, "新建")
                .setIcon(android.R.drawable.ic_menu_send);
        menu.add(Menu.NONE, Menu.FIRST + 2, 3, "取消")
                .setIcon(android.R.drawable.ic_menu_edit);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case Menu.FIRST + 1: {
                final EditText editText = new EditText(fileActivity.this);
                new AlertDialog.Builder(fileActivity.this).setTitle("请输入文件名称").setView(editText).setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                String mFileName = editText.getText().toString();
                                String IMAGES_PATH = Environment.getExternalStorageDirectory() + "/" + mFileName + "/";       //获取根目录
                                //String IMAGES_PATH = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + mFileName + "/";
                                createMkdir(IMAGES_PATH);
                            }
                        }).setNegativeButton("取消", null).show();
            }
            break;
            case Menu.FIRST + 2:
                Toast.makeText(this, "取消", Toast.LENGTH_LONG).show();
                break;
        }
        return false;
    }

    public static void createMkdir(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
