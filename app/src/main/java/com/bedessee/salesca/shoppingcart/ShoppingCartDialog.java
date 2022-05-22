package com.bedessee.salesca.shoppingcart;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bedessee.salesca.R;
import com.bedessee.salesca.customview.ItemType;
import com.bedessee.salesca.database.Database;
import com.bedessee.salesca.main.MainActivity;
import com.bedessee.salesca.modal.ShoppingCartNew;
import com.bedessee.salesca.order.GMailUtils;
import com.bedessee.salesca.orderhistory.OrderHistoryDialog;
import com.bedessee.salesca.sharedprefs.SharedPrefsManager;
import com.bedessee.salesca.store.NewStoreDialog;
import com.bedessee.salesca.store.Store;
import com.bedessee.salesca.store.StoreManager;
import com.bedessee.salesca.store.WebViewer;
import com.bedessee.salesca.utilities.FileUtilities;
import com.bedessee.salesca.utilities.Utilities;
import com.bedessee.salesca.utilities.ViewUtilities;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Dialog for shopping cart.
 */
public class ShoppingCartDialog extends Fragment implements View.OnClickListener {

    final public static int REQUEST_CODE = 200;
    final public static int RESULT_CODE_CONTINUED = 199;
    final public static int RESULT_CODE_CHECKED_OUT = 198;

    final public static String KEY_SHOPPING_CART = "shopping_cart_key";
    public final static String TAG = "ShoppingCart";

    private static ShoppingCartDialog instance;
    public static ShoppingCartDialog getInstance() {
        if (instance == null) {
            instance = new ShoppingCartDialog();
        }
        return instance;
    }
    RecyclerView shoppingCartListView;

    private ShoppingCart mShoppingCart;
    EditText edtComment,edtContact;
    TextView case_amount;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_shopping_cart, container, false);

        final Bundle extras = getArguments();

        if (extras != null && extras.containsKey(KEY_SHOPPING_CART)) {
            mShoppingCart = (ShoppingCart) extras.getSerializable(KEY_SHOPPING_CART);
        } else {
            mShoppingCart = ShoppingCart.getCurrentShoppingCart();
        }

        shoppingCartListView = view.findViewById(R.id.listView_shoppingCart);
        ShoppingCartAdapter shoppingCartAdapter = new ShoppingCartAdapter(getContext(), mShoppingCart.getProducts());
        shoppingCartListView.setLayoutManager(new LinearLayoutManager(getContext()));
        shoppingCartListView.addItemDecoration(new DividerItemDecoration(shoppingCartListView.getContext(), DividerItemDecoration.VERTICAL));
        shoppingCartListView.setItemAnimator(new DefaultItemAnimator());
        shoppingCartListView.setAdapter(shoppingCartAdapter);
        edtComment=(EditText) view.findViewById(R.id.edtComment);
        edtContact=(EditText) view.findViewById(R.id.edtContact);
        case_amount=(TextView) view.findViewById(R.id.case_amount);



        edtComment.setText(mShoppingCart.getComment());
        edtContact.setText(mShoppingCart.getContact());

        String storeName = StoreManager.getCurrentStore().getName();
        if (storeName != null) {
            ((TextView) view.findViewById(R.id.store_name)).setText(storeName);
        }

        view.findViewById(R.id.btnViewStatement).setOnClickListener(this);
//        findViewById(R.id.btnSave).setOnClickListener(this);
        view.findViewById(R.id.btnCheckout).setOnClickListener(this);
        view.findViewById(R.id.btn_close).setOnClickListener(this);

        updateTotal(view);
        updatePrice(view);

        mShoppingCart.setOnShoppingCartChanged(new ShoppingCart.OnShoppingCartChanges() {
            @Override
            public void onChanged() {
                updateTotal(view);
                updatePrice(view);
            }
        });
        return view;
    }


    private void updateTotal(View view) {
        ((TextView) view.findViewById(R.id.totalItems)).setText("Total items: " + mShoppingCart.getTotalItems());
    }

    private void updatePrice(View view){


        double sum=0;
        for(int i=0;i<mShoppingCart.getProducts().size();i++){
                if(mShoppingCart.getProducts().get(i).getItemType()== ItemType.CASE){
                   sum= sum+(double)mShoppingCart.getProducts().get(i).getQuantity()*Double.parseDouble(mShoppingCart.getProducts().get(i).getProduct().getCasePrice());
                }else {
                    List<String> parts= Arrays.asList(mShoppingCart.getProducts().get(i).getProduct().getPiecePrice().toString().split(" "));
                    Log.e("@#@#","get price"+parts);
                    if(parts.size()==2) {
                        String part1 = parts.get(0);
                        String parts2 = parts.get(1);
                        sum = sum + (double) mShoppingCart.getProducts().get(i).getQuantity() * Double.parseDouble(parts2);
                    }else {
                        String part1 = parts.get(0);
                        String parts2 = parts.get(1);
                        sum = sum + (double) mShoppingCart.getProducts().get(i).getQuantity() * Double.parseDouble(parts2);
                    }
                }
            ((TextView) view.findViewById(R.id.order_total_price)).setText("$ " + String.format("%.2f",sum));

        }
        double total_price = Math.round(sum * 100.0) / 100.0;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnViewStatement:
                Store store = StoreManager.getCurrentStore();
                 File file = FileUtilities.Companion.getFile(getContext(),store.getBaseNumber(), "PDF",
                        "r545");

                if (file.exists()) {
                    FileUtilities.Companion.openPDF(getContext(), file);
                }
//                String path = new SharedPrefsManager(ShoppingCartDialog.this).getSugarSyncDir();
//                Store store = StoreManager.getCurrentStore();
//                WebViewer.Companion.show(ShoppingCartDialog.this, path + "/custstmt/" + store.getStatementUrl());
                break;

            case R.id.btn_close:
                saveCommentAndContact();
                //setResult(RESULT_CODE_CONTINUED);
                //finish();
                break;

//            case R.id.btnSave:
//                saveCommentAndContact();
//                setResult(RESULT_CODE_CONTINUED);
//                finish();
//                break;

            case R.id.btnCheckout:

                if (mShoppingCart.isEmpty()) {
                    Utilities.shortToast(getContext(), "Please add a product before checking out");
                } else {
                    saveCommentAndContact();
                    ShoppingCart shoppingCart = mShoppingCart;
                    ShoppingCartNew x = new ShoppingCartNew();
                    //Database.getInstance(getContext()).orderDeo().insertOrder();
                    shoppingCart.stopTimer();
                    ShoppingCart.setCurrentOrderId(getActivity(), null);
                    NewStoreDialog.getInstance().clearAll();
//                    setResult(RESULT_CODE_CHECKED_OUT);
                    GMailUtils.sendShoppingCart(getActivity(), shoppingCart);

                    mShoppingCart.clearProducts();
                    mShoppingCart.clearComment();
                    mShoppingCart.clearContact();

                    StoreManager.clearCurrentStore();
                    StoreManager.setCurrentStore(getContext(),null);

                    //finish();
                }
                break;
            default:
                break;
        }
    }

    /**
     * Saves the comment and the contact that the user typed in.
     */
    private void saveCommentAndContact() {
        mShoppingCart.setComment(edtComment.getText().toString());
        mShoppingCart.setContact(edtContact.getText().toString());
    }
}