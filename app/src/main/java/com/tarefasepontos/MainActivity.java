package br.com.familia.tarefasepontos;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.Intent;
import android.provider.MediaStore;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.UUID;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.text.InputType;
import java.util.ArrayList;

public class MainActivity extends Activity {
    private static final int CAMERA_TASK=101, CAMERA_MEMBER=102;
    private long pendingTask=-1, pendingMember=-1;
    private Database db;
    private LinearLayout root, content;
    private final int green=Color.rgb(41,215,245), dark=Color.rgb(237,244,255), surface=Color.rgb(18,27,54), purple=Color.rgb(159,91,255);

    @Override public void onCreate(Bundle state) { super.onCreate(state); db=new Database(this); showHome(); }
    private int dp(int n) { return (int)(n*getResources().getDisplayMetrics().density+0.5f); }
    private GradientDrawable bg(int color,int radius) { GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));return g; }
    private TextView text(String s,int size,boolean bold) { TextView t=new TextView(this);t.setText(s);t.setTextColor(dark);t.setTextSize(size);if(bold)t.setTypeface(null,Typeface.BOLD);return t; }
    private LinearLayout vertical() { LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l; }
    private void pad(View v,int x,int y) { v.setPadding(dp(x),dp(y),dp(x),dp(y)); }
    private void gap(LinearLayout l,int h) { View v=new View(this);l.addView(v,new LinearLayout.LayoutParams(1,dp(h))); }
    private void start(String title) {
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true);scroll.setBackgroundColor(Color.rgb(7,12,29));
        root=vertical();pad(root,18,22);scroll.addView(root);setContentView(scroll);
        TextView header=text("✦  "+title,26,true);header.setTextColor(green);root.addView(header);gap(root,18);
        LinearLayout nav=new LinearLayout(this);nav.setOrientation(LinearLayout.HORIZONTAL);
        String[] labels={"⌂ Início","🏆 Ranking","◷ Histórico","♧ Família"};
        for(int i=0;i<labels.length;i++){ final int index=i; Button b=new Button(this);b.setText(labels[i]);b.setTextSize(11);b.setAllCaps(false);b.setTextColor(Color.WHITE);b.setBackground(bg(surface,14));nav.addView(b,new LinearLayout.LayoutParams(0,dp(48),1));b.setOnClickListener(v->{if(index==0)showHome();else if(index==1)showRanking();else if(index==2)showHistory();else showFamily();}); }
        root.addView(nav);gap(root,12);content=vertical();root.addView(content);
    }
    private void card(LinearLayout parent,View view) { LinearLayout box=vertical();pad(box,14,13);GradientDrawable panel=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{surface,Color.rgb(29,19,65)});panel.setCornerRadius(dp(18));panel.setStroke(dp(1),Color.rgb(59,92,155));box.setBackground(panel);box.addView(view);parent.addView(box,new LinearLayout.LayoutParams(-1,-2));gap(parent,10); }
    private Button button(String title,Runnable action) { Button b=new Button(this);b.setText(title);b.setAllCaps(false);b.setTextColor(Color.WHITE);GradientDrawable neon=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{Color.rgb(8,117,185),Color.rgb(111,48,197)});neon.setCornerRadius(dp(15));b.setBackground(neon);b.setOnClickListener(v->action.run());return b; }
    private void notifyUser(String s) { Toast.makeText(this,s,Toast.LENGTH_SHORT).show(); }
    private ImageView photo(String path,int size) {
        ImageView image=new ImageView(this);
        image.setLayoutParams(new LinearLayout.LayoutParams(dp(size),dp(size)));
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if(path!=null){ Bitmap bitmap=BitmapFactory.decodeFile(path);if(bitmap!=null)image.setImageBitmap(bitmap); }
        return image;
    }
    private void launchCamera(int request) {
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager())==null){notifyUser("Nenhum aplicativo de câmera disponível");return;}
        try{startActivityForResult(intent,request);}catch(Exception e){notifyUser("Não foi possível abrir a câmera");}
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        if(result!=RESULT_OK||data==null||!(data.getExtras()!=null&&data.getExtras().get("data") instanceof Bitmap))return;
        Bitmap bitmap=(Bitmap)data.getExtras().get("data");
        String path=null;
        try {
            File file=new File(getFilesDir(),"foto_"+UUID.randomUUID()+".jpg");
            try(FileOutputStream out=new FileOutputStream(file)){bitmap.compress(Bitmap.CompressFormat.JPEG,85,out);}
            path=file.getAbsolutePath();
        }catch(Exception e){notifyUser("Erro ao salvar foto");return;}
        if(request==CAMERA_TASK&&pendingTask>=0){db.complete(pendingTask,path);pendingTask=-1;showHome();}
        else if(request==CAMERA_MEMBER&&pendingMember>=0){db.setMemberPhoto(pendingMember,path);pendingMember=-1;showFamily();}
    }
    private void captureTask(long id){pendingTask=id;launchCamera(CAMERA_TASK);}
    private void captureMember(long id){pendingMember=id;launchCamera(CAMERA_MEMBER);}
    private void showHome() {
        start("Tarefas & Pontos");
        TextView summary=text("✦  OLÁ, FAMÍLIA!\n\nHoje: "+db.todayCount()+" tarefa(s) concluída(s)\n⭐ "+db.todayPoints()+" pontos conquistados",19,true);summary.setTextColor(green);card(content,summary);
        content.addView(button("+ Cadastrar familiar",this::newMember));
        content.addView(button("+ Criar tarefa",this::newTask));gap(content,16);
        content.addView(text("✦  Missões do dia",21,true));gap(content,10);
        boolean has=false;
        try(Cursor c=db.tasks()) {
            while(c.moveToNext()) {
                has=true;long id=c.getLong(0);String title=c.getString(1),name=c.getString(3);int points=c.getInt(2);boolean done=c.getInt(4)==1;
                LinearLayout item=vertical();item.addView(text((done?"✓  ":"○  ")+title,18,true));item.addView(text(name+"  •  "+points+" pontos"+(done?"  •  Feita hoje":""),14,false));
                LinearLayout actions=new LinearLayout(this);
                Button toggle=button(done?"Desfazer":"📷 Fotografar e concluir",()->{if(done){db.undoToday(id);showHome();}else captureTask(id);});actions.addView(toggle,new LinearLayout.LayoutParams(0,dp(50),2));
                Button remove=button("Excluir",()->new AlertDialog.Builder(this).setTitle("Excluir tarefa?").setMessage("O histórico de pontos já ganhos será preservado.").setNegativeButton("Cancelar",null).setPositiveButton("Excluir",(d,w)->{db.deleteTask(id);showHome();}).show());actions.addView(remove,new LinearLayout.LayoutParams(0,dp(50),1));item.addView(actions);if(done&&!c.isNull(5))item.addView(photo(c.getString(5),110));card(content,item);
            }
        }
        if(!has)card(content,text("Cadastre um familiar e crie a primeira tarefa.",15,false));
        card(content,text("Para concluir uma tarefa, tire uma foto como registro. A câmera salva uma imagem reduzida. Cada tarefa vale pontos uma vez por dia.",13,false));
    }
    private void newMember() {
        EditText input=new EditText(this);input.setSingleLine(true);input.setHint("Nome do familiar");
        LinearLayout wrap=vertical();pad(wrap,20,5);wrap.addView(input);
        new AlertDialog.Builder(this).setTitle("Novo familiar").setView(wrap).setNegativeButton("Cancelar",null).setPositiveButton("Salvar",(d,w)->{String name=input.getText().toString().trim();if(name.isEmpty()){notifyUser("Informe um nome");return;}db.addMember(name);showHome();}).show();
    }
    private void newTask() {
        ArrayList<Long> ids=new ArrayList<>();ArrayList<String> names=new ArrayList<>();
        try(Cursor c=db.members()){while(c.moveToNext()){ids.add(c.getLong(0));names.add(c.getString(1));}}
        if(ids.isEmpty()){new AlertDialog.Builder(this).setMessage("Cadastre um familiar antes de criar tarefas.").setPositiveButton("OK",null).show();return;}
        LinearLayout wrap=vertical();pad(wrap,20,8);
        EditText title=new EditText(this);title.setHint("Ex.: Lavar a louça");title.setSingleLine(true);wrap.addView(title);
        EditText points=new EditText(this);points.setHint("Pontos (ex.: 20)");points.setInputType(InputType.TYPE_CLASS_NUMBER);wrap.addView(points);
        TextView label=text("Responsável:",14,true);wrap.addView(label);
        Spinner spinner=new Spinner(this);ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,names);spinner.setAdapter(adapter);wrap.addView(spinner);
        new AlertDialog.Builder(this).setTitle("Nova tarefa").setView(wrap).setNegativeButton("Cancelar",null).setPositiveButton("Salvar",(d,w)->{
            String name=title.getText().toString().trim();int score=0;try{score=Integer.parseInt(points.getText().toString());}catch(NumberFormatException ignored){}
            if(name.isEmpty()||score<1||score>100000){notifyUser("Informe uma tarefa e pontos entre 1 e 100000");return;}
            db.addTask(name,score,ids.get(spinner.getSelectedItemPosition()));showHome();
        }).show();
    }
    private void showFamily(){
        start("♧ Nossa família");
        content.addView(button("+ Cadastrar familiar",this::newMember));
        try(Cursor c=db.members()){
            while(c.moveToNext()){
                long id=c.getLong(0);String name=c.getString(1);String path=c.getString(2);
                LinearLayout item=vertical();item.addView(text(name,19,true));
                if(path!=null)item.addView(photo(path,100));
                item.addView(button("📷 Tirar foto de "+name,()->captureMember(id)));card(content,item);
            }
        }
    }
    private void showRanking() {
        start("🏆 Ranking da família");
        content.addView(text("⭐ Pontuação total",20,true));gap(content,8);
        int place=1;boolean has=false;
        try(Cursor c=db.ranking()){while(c.moveToNext()){has=true;LinearLayout item=vertical();item.addView(text(place+"º  "+c.getString(0)+"   ⭐ "+c.getInt(1)+" pontos",19,true));
            if(!c.isNull(2))item.addView(photo(c.getString(2),72));card(content,item);place++;}}
        if(!has)card(content,text("Nenhum familiar cadastrado.",15,false));
        Calendar cal=Calendar.getInstance();cal.setFirstDayOfWeek(Calendar.MONDAY);
        int day=cal.get(Calendar.DAY_OF_WEEK);int offset=(day+5)%7;cal.add(Calendar.DAY_OF_MONTH,-offset);
        SimpleDateFormat fmt=new SimpleDateFormat("yyyy-MM-dd",Locale.US);
        String monday=fmt.format(cal.getTime());cal.add(Calendar.DAY_OF_MONTH,6);String sunday=fmt.format(cal.getTime());
        gap(content,10);content.addView(text("🏆 Ranking desta semana",20,true));gap(content,8);
        int weekPlace=1;
        try(Cursor c=db.weeklyRanking(monday,sunday)){
            while(c.moveToNext()){card(content,text(weekPlace+"º  "+c.getString(0)+"  •  "+c.getInt(1)+" pontos",17,true));weekPlace++;}
        }
        card(content,text("Semana: "+monday+" a "+sunday+". O total geral preserva pontos de tarefas excluídas.",13,false));
    }
    private void showHistory() {
        start("◷ Histórico");boolean has=false;
        try(Cursor c=db.history()){while(c.moveToNext()){has=true;LinearLayout item=vertical();item.addView(text(c.getString(0)+"  •  "+c.getString(3)+"\n"+c.getString(1)+"   +"+c.getInt(2)+" pontos",16,false));
            if(!c.isNull(4))item.addView(photo(c.getString(4),160));card(content,item);}}
        if(!has)card(content,text("Ainda não há tarefas concluídas.",15,false));
        card(content,text("Exibindo até 100 conclusões recentes.",13,false));
    }
    @Override protected void onDestroy(){db.close();super.onDestroy();}
}
