package aarsy.github.com.ez_vcard_android;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ezvcard.VCard;
import ezvcard.android.AndroidCustomFieldScribe;
import ezvcard.android.ContactOperations;
import ezvcard.io.text.VCardReader;

import static ezvcard.util.IOUtils.closeQuietly;


/**
 * Created by abhay yadav on 10-Jan-17.
 */
public class VCFfilesListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, MyAlertDialog.ItemAccountSelectedListener {

    private static final String TAG = "VCFfilesListActivity";
    private static final int DIALOG_COPY = 1;
    private VCFFilesAdapter adapter;
    private RecyclerView recyclerView;
    private List<VCFFiles> allFilesList;
    private Context context;
    private static final int LOADER_ID = 1;
    private int selectedFilePositioninList = 0;
    private RelativeLayout rlNoContacts;
    private final int REQUEST_CODE_PERMISSION_READ_EXT_STORAGE = 100;
    private final int REQUEST_CODE_PERMISSION_WRITE_CONTACTS = 101;
    private String filePath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vcffiles_list);
        context = this;
        if (savedInstanceState != null) {
            filePath = savedInstanceState.getString("filePath");
        }
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        rlNoContacts = (RelativeLayout) findViewById(R.id.rl_no_contacts_in_list);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.mipmap.ic_launcher);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        askPermissionAndFindFiles(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_CODE_PERMISSION_READ_EXT_STORAGE, getString(R.string.you_need_to_allow_access_to_ext_storage));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("filePath", filePath);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }



    @Override
    protected void onResume() {
        super.onResume();
    }

    private void askPermissionAndFindFiles(final String permissionType, final int permissionCode, String message) {
        int hasExternalStoragePermission = ContextCompat.checkSelfPermission(context, permissionType);
        if (hasExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permissionType)) {
                showMessageOkCancel(message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context, new String[]{permissionType}, permissionCode);
                    }
                });
                return;
            }
            ActivityCompat.requestPermissions((Activity) context, new String[]{permissionType}, permissionCode);
            return;
        }
        if (permissionCode == REQUEST_CODE_PERMISSION_READ_EXT_STORAGE)
            getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        else
            //for writing contacts
            getAccountsAndShowDialog();
    }


    private void showMessageOkCancel(String message, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("Ok", listener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_READ_EXT_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getSupportLoaderManager().initLoader(LOADER_ID, null, this);
                } else {
                    Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_CODE_PERMISSION_WRITE_CONTACTS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    private void getAccountsAndShowDialog() {
        AccountManager am = AccountManager.get(this);

        //Since write contacts permission is already asked and GET_ACCOUNTS permission also lies
        // in same permission group therefore there is no need to ask for it. It will be allowed automatically.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Account[] accounts = am.getAccounts();
        String account_name = null;
        String account_type = null;
        for (Account ac : accounts) {
            String acname = ac.name;
            String actype = ac.type;
            System.out.println("Accounts : " + acname + ", " + actype);
            //get the google account
            if (actype.equals("com.google")) {
                account_name = acname;
                account_type = actype;
            }
        }
        FragmentManager fragmentm = getSupportFragmentManager();
        MyAlertDialog dialog = MyAlertDialog.newInstance(account_name, account_type);
        dialog.show(fragmentm, "fragment_select");
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        allFilesList = new ArrayList<>();
        if (id == LOADER_ID) {
            Uri URI = MediaStore.Files.getContentUri("external");
            //get all files from external storage(both phones memory and SD card's memory) that ends with .vcf extension
            String selection = MediaStore.Files.FileColumns.DATA + " like '%.vcf' or " + MediaStore.Files.FileColumns.DATA + " like '&.VCF'";
            return new CursorLoader(this, URI, null, selection, null, MediaStore.Images.ImageColumns.DATE_MODIFIED);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //      progressDialog.dismiss();
        if (cursor != null) {
            Log.d("cursorcount", " " + cursor.getCount() + " " + cursor.getColumnCount());
            if (cursor.moveToFirst()) {
                do {
                    String file_name = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME));
                    int file_id = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
                    String file_fullpath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                    if (file_name == null)
                        file_name = file_fullpath.substring(file_fullpath.lastIndexOf('/') + 1);
                    VCFFiles vcfFiles = new VCFFiles(file_id, file_name, file_fullpath);
                    allFilesList.add(vcfFiles);
                    System.out.println("namessss:   " + file_name + "     " + file_id + "    " + file_fullpath);
                } while (cursor.moveToNext());

            }
            if (!allFilesList.isEmpty()) {
                recyclerView.setVisibility(View.VISIBLE);
                rlNoContacts.setVisibility(View.GONE);
                adapter = new VCFFilesAdapter(context);
                Log.d("vcffilelistSize", " " + allFilesList.size());
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setAdapter(adapter);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }


    public void readVCFAndWrite(Context context, String path, String acc_name, String acc_type) {


        //Log.d("insertToPhoneclick", "true "+path+" "+acc_name+" "+acc_type);
        File vcardFile = new File(path);
        if (!vcardFile.exists()) {
            throw new RuntimeException("vCard file does not exist: " + path);
        }
        VCardReader reader = null;
        try {
            reader = new VCardReader(vcardFile);
            reader.registerScribe(new AndroidCustomFieldScribe());
            ContactOperations operations = new ContactOperations(getApplicationContext(), acc_name, acc_type);
            VCard vcard = null;
            while ((vcard = reader.readNext()) != null) {
                //inserts contacts automatically
                operations.insertContact(vcard);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuietly(reader);
        }
    }


    public void readVCFAndWriteToSimCard(Context context, String path, String acc_name, String acc_type) {

        Log.d("insertTosimclick", "true "+path);
        //1).inserting into sim card using the basic approach.

        //2).You can also use same method which is used while inserting into phone or gmail account if and only if there exists an SIM card
        // accounts in while getting all acounts like we have done for getting gmail account
        File vcardFile = new File(path);
        Uri simUri = Uri.parse("content://icc/adn");
        ContentValues cv = new ContentValues();
        if (!vcardFile.exists()) {
            throw new RuntimeException("vCard file does not exist: " + path);
        }
        VCardReader reader = null;
        try {
            reader = new VCardReader(vcardFile);
            reader.registerScribe(new AndroidCustomFieldScribe());
            VCard vcard = null;
            while ((vcard = reader.readNext()) != null) {
                //I'm inserting only one contact here
                try {
                    if (vcard.getFormattedName().getValue() != null && !vcard.getTelephoneNumbers().isEmpty()) {
                        cv = new ContentValues();
                        cv.put("tag", vcard.getFormattedName().getValue());
                        cv.put("number", vcard.getTelephoneNumbers().get(0).getText());
                        getContentResolver().insert(simUri, cv);
                    }
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuietly(reader);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            getSupportLoaderManager().destroyLoader(LOADER_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccountSelected(String account_name, String account_type) {
        SaveContacts saveContacts;
        if (account_name.equals("SIMCARD")) {
            saveContacts = new SaveContacts();
            saveContacts.execute(null, null, true);
        } else if (account_name.equals("Phone")) {
            saveContacts = new SaveContacts();
            //account_name and account_type are null for phone
            saveContacts.execute(null, null, false);
        } else {
            saveContacts = new SaveContacts();
            //google account_name and account_type fetched before
            saveContacts.execute(account_name, account_type, false);
        }
    }


    ProgressDialog progressDialog;


    private class SaveContacts extends AsyncTask<Object, Integer, Void> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(context, "", "Saving contacts");

        }

        public void doProgress(int progress) {
            Log.d("prrrogress", " " + progress);
            publishProgress(progress);
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            //super.onProgressUpdate(values);
            Log.d("progressValues ", " " + values[0]);
            progressDialog.setProgress((int) values[0]);
        }

        @Override
        protected Void doInBackground(Object... params) {
            String accname = (String) params[0];
            String acctype = (String) params[1];
            boolean savetosim = (boolean) params[2];
            if (savetosim)
                insertToSimCard(selectedFilePositioninList);
            else
                insertToPhoneOrGmailAccount(accname, acctype, selectedFilePositioninList);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            Toast.makeText(context, "Contacts Inserted", Toast.LENGTH_SHORT).show();
        }
    }

    private void insertToPhoneOrGmailAccount(String account_name, String account_type, int position) {
        readVCFAndWrite(context, allFilesList.get(position).getFilePath(), account_name, account_type);

    }



    private void insertToSimCard(int position) {

            readVCFAndWriteToSimCard(context, allFilesList.get(position).getFilePath(), null, null);
    }


    private class MyViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        TextView fileName, path, serialNo;
        VCFFiles data;

        public MyViewHolder(View view) {
            super(view);
            fileName = (TextView) view.findViewById(R.id.file_name);
            path = (TextView) view.findViewById(R.id.file_path);
            serialNo = (TextView) view.findViewById(R.id.serial);
            view.setOnClickListener(this);
        }

        public void bindUser(VCFFiles data, int position) {
            this.data = data;
            fileName.setText(data.getFileName());
            path.setText(data.getFilePath());
            serialNo.setText((position + 1) + ".");
        }

        @Override
        public void onClick(View v) {
            filePath = data.getFilePath();
            selectedFilePositioninList = this.getAdapterPosition();
            askPermissionAndFindFiles(Manifest.permission.WRITE_CONTACTS, REQUEST_CODE_PERMISSION_WRITE_CONTACTS, getString(R.string.allow_access_to_write_contacts));
        }


    }

    private class VCFFilesAdapter extends RecyclerView.Adapter<MyViewHolder> {
        Context context;

        public VCFFilesAdapter(Context context) {
            this.context = context;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.single_item_vcflist, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final VCFFiles data = allFilesList.get(position);
            holder.bindUser(data, position);
        }

        @Override
        public int getItemCount() {
            return allFilesList.size();
        }
    }
}
