package com.twincoders.twinpush.sdk.demo;

import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.activities.RichNotificationActivity;
import com.twincoders.twinpush.sdk.communications.TwinRequest;
import com.twincoders.twinpush.sdk.communications.requests.notifications.GetInboxRequest;
import com.twincoders.twinpush.sdk.demo.adapters.DividerItemDecoration;
import com.twincoders.twinpush.sdk.demo.adapters.InboxAdapter;
import com.twincoders.twinpush.sdk.demo.adapters.ItemClickSupport;
import com.twincoders.twinpush.sdk.entities.InboxNotification;
import com.twincoders.twinpush.sdk.logging.Ln;
import com.twincoders.twinpush.sdk.services.NotificationIntentService;

import java.util.ArrayList;
import java.util.List;

/**
 * Example of User Inbox using a Recycler view with InboxAdapter.
 * In this demo, long click over a notification will remove it from the inbox
 */
public class InboxActivity extends ParentActivity implements ItemClickSupport.OnItemClickListener,
        ItemClickSupport.OnItemLongClickListener{

    private RecyclerView mRecyclerView;
    private InboxAdapter mAdapter;

    View progressView;
    View emptyView;

    View displayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        progressView = findViewById(R.id.progress_bar);
        emptyView = findViewById(R.id.empty_view);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new InboxAdapter(this, new ArrayList<InboxNotification>());
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(this);
        ItemClickSupport.addTo(mRecyclerView).setOnItemLongClickListener(this);

        reload(null);
    }

    public void reload(MenuItem object) {
        showProgress();
        TwinPushSDK.getInstance(this).getUserInbox(0, 50, new GetInboxRequest.Listener() {
            @Override
            public void onSuccess(List<InboxNotification> notifications, int totalPages) {
                mAdapter.setNotifications(notifications);
                mAdapter.notifyDataSetChanged();
                if (notifications.isEmpty()) {
                    stopProgress(emptyView);
                } else {
                    stopProgress(mRecyclerView);
                }
            }

            @Override
            public void onError(Exception exception) {
                Toast.makeText(InboxActivity.this, String.format("Error while trying to load notifications: %s", exception.getLocalizedMessage()), Toast.LENGTH_LONG).show();
                stopProgress(emptyView);
            }
        });
    }

    @Override
    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
        InboxNotification notification = mAdapter.getNotifications().get(position);
        if (notification.getNotification().isRichNotification()) {
            Intent richIntent = new Intent(this, RichNotificationActivity.class);
            richIntent.putExtra(NotificationIntentService.EXTRA_NOTIFICATION, notification.getNotification());
            startActivity(richIntent);
        }
    }

    @Override
    public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {
        InboxNotification notification = mAdapter.getNotifications().get(position);
        TwinPushSDK.getInstance(this).deleteNotification(notification, new TwinRequest.DefaultListener() {
            @Override
            public void onSuccess() {
                reload(null);
            }

            @Override
            public void onError(Exception exception) {
                Ln.e(exception, "Could not delete notification");
            }
        });
        return false;
    }

    private void showProgress() {
        super.showProgress(true, progressView, displayView != null ? displayView : mRecyclerView);
    }

    private void stopProgress(View targetView) {
        this.displayView = targetView;
        super.showProgress(false, progressView, displayView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.inbox_menu, menu);
        return true;
    }
}
