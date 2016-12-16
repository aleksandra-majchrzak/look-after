package com.hfad.lookafter.bookslists;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.hfad.lookafter.R;
import com.hfad.lookafter.content.ContentActivity;
import com.hfad.lookafter.database.ConnectionManager;

public class BooksListFragment extends ListFragment {

    private ConnectionManager connectionManager = new ConnectionManager();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // do tej pory korzystalas z domyslnej implementacji ListFragment, a teraz ladujemy tu
        // customowy layout - zastanow sie czy nie lepiej z tego zrobic normalny fragment
        // tak jak jest teraz jest troche niebezpiecznie, bo implementacja ListFragment odwoluje sie
        // np do id wewnetrznych widokow (listView w customowym layoucie musi miec takie samo id jak to domyslne)

        View view = inflater.inflate(R.layout.books_list_fragment,
                container, false); /*super.onCreateView(inflater, container, savedInstanceState);*/
        setHasOptionsMenu(true);
        generateList();

        return view;
    }

    private void generateList() {
        new ListGenerator(getActivity()).execute();
    }

    private class ListGenerator extends AsyncTask<Void, Cursor, Boolean> {

        private Context context;

        public ListGenerator(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Cursor cursor = connectionManager.getAllBooks();
                publishProgress(cursor);
                return true;
            } catch (SQLiteException ex) {
                ex.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(Cursor... cursors) {
            Cursor cursor = cursors[0];

            com.hfad.lookafter.adapters.CursorAdapter customAdapter = new com.hfad.lookafter.adapters.CursorAdapter(
                    context, cursor, 0);

            setListAdapter(customAdapter);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                connectionManager.showPrompt(getActivity());
            }
        }
    }

    @Override
    public void onListItemClick(ListView listView, View itemView, int position, long id) {
        Intent intent = new Intent(getActivity(), ContentActivity.class);
        intent.putExtra(ContentActivity.EXTRA_BOOKN0, (int) id);
        startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_favourite, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favourite_list:
                Intent intent = new Intent(getActivity(), FavouriteListActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        connectionManager.close();
    }
}