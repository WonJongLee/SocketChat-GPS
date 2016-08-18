package wj.mh.chat.Adapter;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.sql.SQLClientInfoException;
import java.util.ArrayList;

import wj.mh.chat.ChatData.ChatMessage;
import wj.mh.chat.R;


/**
 * Created by wj629 on 2015-12-10.
 */


import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Technovibe on 17-04-2015.
 */
public class MyAdapter extends BaseAdapter {

    public static List<ChatMessage> chatMessages = null;
    private Activity context;


    public MyAdapter(Activity context, List<ChatMessage> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
    }

    @Override
    public int getCount() {
        if (chatMessages != null) {
            return chatMessages.size();
        } else {
            return 0;
        }
    }

    @Override
    public ChatMessage getItem(int position) {
        if (chatMessages != null) {
            return chatMessages.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        ChatMessage chatMessage = getItem(position);
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = vi.inflate(R.layout.list_item_chat_message, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        boolean myMsg = chatMessage.getIsme() ;//Just a dummy check to simulate whether it me or other sender
        setAlignment(holder, myMsg);
        holder.txtMessage.setText(chatMessage.getMessage());
        holder.txtInfo.setText(chatMessage.getDate());


        return convertView;
    }

    public void add(ChatMessage message) {
        chatMessages.add(message);
    }

    public void add(List<ChatMessage> messages) {
        chatMessages.addAll(messages);
    }

    private void setAlignment(ViewHolder holder, boolean isMe) {
        if (!isMe) {
            holder.contentWithBG.setBackgroundResource(R.drawable.out_message_bg);

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            holder.content.setLayoutParams(lp);
            layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.txtMessage.setLayoutParams(layoutParams);

            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.txtInfo.setLayoutParams(layoutParams);
        } else {
            holder.contentWithBG.setBackgroundResource(R.drawable.in_message_bg);

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            holder.content.setLayoutParams(lp);
            layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.txtMessage.setLayoutParams(layoutParams);

            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.txtInfo.setLayoutParams(layoutParams);
        }
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.txtMessage = (TextView) v.findViewById(R.id.txtMessage);
        holder.content = (LinearLayout) v.findViewById(R.id.content);
        holder.contentWithBG = (LinearLayout) v.findViewById(R.id.contentWithBackground);
        holder.txtInfo = (TextView) v.findViewById(R.id.txtInfo);
        return holder;
    }


    private static class ViewHolder {
        public TextView txtMessage;
        public TextView txtInfo;
        public LinearLayout content;
        public LinearLayout contentWithBG;
    }
}








































//
//public class MyAdapter extends ArrayAdapter<String> {
//    public static ArrayList<Bitmap> bit = new ArrayList<Bitmap>();
//    public MyAdapter(Context context, int resource, ArrayList<String> list) {
//        super(context, resource, list);
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        // Check if an existing view is being reused, otherwise inflate the view
//        if (convertView == null) {
//            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_chat_message, parent, false);
//        }
//        // Lookup view for data population
//        TextView listItemText = (TextView) convertView.findViewById(R.id.listItemText);
//        LinearLayout ll = (LinearLayout) convertView.findViewById(R.id.lLayout);
//        ImageView iv = new ImageView(getContext());
//        // Populate the data into the template view using the data object
////        listItemText.setText(MyArray.myData.get(position));
//        // Return the completed view to render on screen
////        if(!MyArray.myData.get(position).equals(""))
//
//        int i=0;
//        if(MyArray.myData.get(position).equals("")) {
//        }  else if(MyArray.myData.get(position).equals("[Image 전송]")) {
//            iv.setLayoutParams(new ActionBar.LayoutParams(150, 150));
//            iv.setImageBitmap(bit.get(i));
//            i++;
//            ll.addView(iv);
//            MyArray.myData.remove(position);
//        } else {
//            ll.removeView(iv);
//            String[] str = MyArray.myData.get(position).split(":");
////            if(str[0].equals("[낯선사람] ")) {
////                listItemText.setText(MyArray.myData.get(position).substring(2));
////                ll.setGravity(Gravity.RIGHT);
////
////            } else if(str[0].equals("YOU")) {
////                listItemText.setText(MyArray.myData.get(position).substring(4));
////                ll.setGravity(Gravity.LEFT);
////            }
//
//            listItemText.setText(MyArray.myData.get(position));
//
//        }
//
//
//
//
//
//
//
//
////        if(MyImageArray.myImgData.size() != 0) {
////            listItemImage.setVisibility(View.VISIBLE);
////            listItemImage.setImageBitmap(MyImageArray.myImgData.get(i));
////            i++;
////        }
//
//
//        return convertView;
//    }
//
//
//}