package org.qii.weiciyuan.ui.userinfo;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.show.ShowUserDao;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.utils.ListViewTool;
import org.qii.weiciyuan.ui.Abstract.*;
import org.qii.weiciyuan.ui.browser.SimpleBitmapWorkerTask;

/**
 * User: qii
 * Date: 12-7-30
 */
public class MyInfoFragment extends Fragment {

    private UserBean bean;

    private ImageView avatar;

    private TextView username;
    private TextView verified_reason;
    private TextView isVerified;
    private TextView info;
    private TextView blog_url;
    private TextView location;
    private TextView sex;
    private TextView following_number;
    private TextView fans_number;
    private TextView fav_number;

    private View verified_layout;
    private View intro_layout;
    private View location_layout;
    private View blog_url_layout;

    protected ICommander commander;

    private AsyncTask<Object, UserBean, UserBean> task;


    public MyInfoFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bean = ((IUserInfo) getActivity()).getUser();
        commander = ((AbstractAppActivity) getActivity()).getCommander();
        setValue();
        refresh();
    }

    @Override
    public void onStart() {
        super.onStart();
        // new SimpleTask().execute();
    }

    private void setValue() {
        username.setText(bean.getScreen_name());

        if (bean.isVerified()) {
            isVerified.setVisibility(View.VISIBLE);
            isVerified.setText(getString(R.string.verified_user));
            verified_reason.setText(bean.getVerified_reason());
        } else {
            verified_layout.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(bean.getDescription())) {
            info.setText(bean.getDescription());
        } else {
            intro_layout.setVisibility(View.GONE);
        }

        String avatarUrl = bean.getAvatar_large();
        if (!TextUtils.isEmpty(avatarUrl)) {
            new SimpleBitmapWorkerTask(avatar, FileLocationMethod.avatar_large).execute(avatarUrl);
        }
        if (!TextUtils.isEmpty(bean.getUrl())) {

            blog_url.setText(bean.getUrl());
            ListViewTool.addLinks(blog_url);
        } else {
            blog_url_layout.setVisibility(View.GONE);
            blog_url.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(bean.getLocation())) {
            location.setText(bean.getLocation());
        } else {
            location_layout.setVisibility(View.GONE);
        }
        String s = bean.getGender();
        if (!TextUtils.isEmpty(s)) {
            if (s.equals("m"))
                sex.setText(getString(R.string.m));
            else if (s.equals("f"))
                sex.setText(getString(R.string.f));
            else
                sex.setVisibility(View.GONE);
        }

        setTextViewNum(fans_number, bean.getFollowers_count());
        setTextViewNum(following_number, bean.getFriends_count());
        setTextViewNum(fav_number, bean.getFavourites_count());
        getActivity().getActionBar().getTabAt(1).setText(getString(R.string.weibo) + "(" + bean.getStatuses_count() + ")");

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_myinfo_layout, container, false);
        avatar = (ImageView) view.findViewById(R.id.avatar);

        username = (TextView) view.findViewById(R.id.username);
        isVerified = (TextView) view.findViewById(R.id.isVerified);
        verified_reason = (TextView) view.findViewById(R.id.verified_info);
        info = (TextView) view.findViewById(R.id.textView_info);
        blog_url = (TextView) view.findViewById(R.id.blog_url);
        location = (TextView) view.findViewById(R.id.location);
        sex = (TextView) view.findViewById(R.id.sex);
        following_number = (TextView) view.findViewById(R.id.following_number);
        fans_number = (TextView) view.findViewById(R.id.fans_number);
        fav_number=(TextView)view.findViewById(R.id.fav_number);

        blog_url_layout = view.findViewById(R.id.blog_url_layout);
        intro_layout = view.findViewById(R.id.intro_layout);
        location_layout = view.findViewById(R.id.location_layout);
        verified_layout = view.findViewById(R.id.verified_layout);

        View fan_layout = view.findViewById(R.id.fan_layout);
        View following_layout = view.findViewById(R.id.following_layout);
        View fav_layout = view.findViewById(R.id.fav_layout);

        following_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FriendListActivity.class);
                intent.putExtra("token", ((IToken) getActivity()).getToken());
                intent.putExtra("user", bean);
                startActivity(intent);
            }
        });
        fan_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FanListActivity.class);
                intent.putExtra("token", ((IToken) getActivity()).getToken());
                intent.putExtra("user", bean);
                startActivity(intent);
            }
        });
        fav_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MyFavActivity.class);
                intent.putExtra("token", ((IToken) getActivity()).getToken());
                startActivity(intent);
            }
        });
        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actionbar_menu_myinfofragment, menu);

    }


    @Override
    public void onDetach() {
        super.onDetach();
        if (task != null)
            task.cancel(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                refresh();
                break;


        }
        return true;
    }

    private void refresh() {
        if (task == null || task.getStatus() == AsyncTask.Status.FINISHED) {
            task = new SimpleTask();
            task.execute();
        }
    }

    private class SimpleTask extends AsyncTask<Object, UserBean, UserBean> {
        WeiboException e;

        @Override
        protected UserBean doInBackground(Object... params) {
            UserBean user = null;
            try {
                ShowUserDao dao = new ShowUserDao(((IToken) getActivity()).getToken());
                if (!TextUtils.isEmpty(bean.getId()))
                    dao.setUid(bean.getId());
                else
                    dao.setScreen_name(bean.getScreen_name());

                user = dao.getUserInfo();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
            }
            if (user != null) {
                bean = user;
                DatabaseManager.getInstance().updateAccountMyInfo(((IAccountInfo) getActivity()).getAccount(), bean);
            } else {
                cancel(true);
            }
            return user;
        }

        @Override
        protected void onCancelled(UserBean userBean) {
            super.onCancelled(userBean);
            if (e != null && getActivity() != null) {
                Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPostExecute(UserBean o) {

            setValue();
            super.onPostExecute(o);
        }
    }

    private void setTextViewNum(TextView tv, String num) {

//        String name = tv.getText().toString();
//        String value = "(" + num + ")";
//        if (!name.endsWith(")")) {
//            tv.setText(name + value);
//        } else {
//            int index = name.indexOf("(");
//            String newName = name.substring(0, index);
//            tv.setText(newName + value);
//        }
        tv.setText(num);

    }
}
