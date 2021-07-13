package pace.cs639.healthyshopper;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link shopping_list#newInstance} factory method to
 * create an instance of this fragment.
 */
public class shopping_list extends Fragment {

    private static ArrayList<SL_FoodItem> foodList;

    private RecyclerView SL_recycler;
    private TextInputEditText foodNameInput;
    private TextInputEditText qtyInput;
    private Button addBtn;
    private Button delBtn;
    FirebaseDatabase database;
    DatabaseReference myRef;


    public shopping_list() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static shopping_list newInstance(String param1, String param2) {
        shopping_list fragment = new shopping_list();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        foodList = new ArrayList<>();
        //default test food items
        //foodList.add(new SL_FoodItem("Apple", 2));
        //foodList.add(new SL_FoodItem("Banana", 3));
    }

    private void setAdapter(AddFoodItemAdapter adapter) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        SL_recycler.setLayoutManager(layoutManager);
        SL_recycler.setItemAnimator(new DefaultItemAnimator());
        SL_recycler.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shopping_list, container, false);
        SL_recycler = (RecyclerView) view.findViewById(R.id.shopping_list_recycler);
        AddFoodItemAdapter adapter = new AddFoodItemAdapter(foodList);
        setAdapter(adapter);
        foodNameInput = (TextInputEditText) view.findViewById(R.id.SL_FoodInput);
        qtyInput = (TextInputEditText) view.findViewById(R.id.SL_QtyInput);
        delBtn = (Button) view.findViewById(R.id.SL_DeleteBtn);
        addBtn = (Button) view.findViewById(R.id.SL_AddItemBtn);

        //Database code starts here
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child("pantry_items");
        Log.i("MAINACTIVITY", myRef.toString());

        //Database listener
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Pantry_Item item;
                int counter = 0;
                //reset list so that it can load the database on datachange
                foodList.clear();
                adapter.notifyDataSetChanged();

                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    item= (Pantry_Item) ds.getValue(Pantry_Item.class);
                    Log.i("Shopping", counter + " - Name: " + item.getName()+ "Nutrition: " + item.getNutrition()+ "Max: " +
                            item.getMax() + "Total: " + item.getTotal());
                    String food_name = item.getName();
                    String max_item = item.getMax();
                    String total_item =item.getTotal();
                    String food_ntr = item.getNutrition();
                    String [] cal = food_ntr.split("Calories: ");
                    Log.i("Calorie split", cal[1]);
                    int qty = Integer.parseInt(max_item)-Integer.parseInt(total_item);
                    double total_calories = Double.parseDouble(cal[1].trim())*qty;
                    String str_calories = String.valueOf(total_calories);
                    SL_FoodItem food = new SL_FoodItem();
                    food.setCal(str_calories);
                    food.setName(food_name);
                    food.setQty(qty);
                    foodList.add(food);
                    adapter.notifyItemInserted(foodList.size() - 1);

                    //pantryList.add(new Pantry_Item(food_name, food_ntr,max_item, total_item));
                    //adapter.notifyItemInserted(pantryList.size() - 1);

                    counter++;
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("MAINACTIVITY", "Failed to read value.", error.toException());
            }
        });
        //Database code ends

        addBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                InputMethodManager inputManager = (InputMethodManager)
                        getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputManager != null) {
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }

                String fName = foodNameInput.getText().toString().trim();
                String qty = qtyInput.getText().toString().trim();
                if (fName.length() != 0 && qty.length() != 0){
                    int found = 0;
                    for (int i = 0; i < foodList.size(); i++){
                        if (foodList.get(i).getName().equals(fName)){
                            found = 1;
                            break;
                        }
                    }
                    if (found == 1){
                        Toast.makeText(getContext(),"Food already in list!",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        foodList.add(new SL_FoodItem(fName, Integer.parseInt(qty)));
                        adapter.notifyItemInserted(foodList.size() - 1);
                        Toast.makeText(getContext(),"Shopping list updated!",Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });

        return view;
    }
}