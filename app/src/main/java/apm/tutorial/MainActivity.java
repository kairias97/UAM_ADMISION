/*
 * John Granados, Kevin Irías, Dustin Espinoza y Carlos Ortega (c) 2016.
 */


package apm.tutorial;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;


public class MainActivity extends Activity {

    public EditText nombres;
    public EditText email;
    public EditText telefono;
    public TextView textView;
    public Button save, loadPend, sync, loadSub;


    public String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nombres = (EditText) findViewById(R.id.nombres);
        email = (EditText) findViewById(R.id.email);
        telefono = (EditText) findViewById(R.id.telefono);
        textView = (TextView) findViewById(R.id.textView);
        save = (Button) findViewById(R.id.save);
        loadPend = (Button) findViewById(R.id.loadPend);
        loadSub = (Button) findViewById(R.id.loadSub);
        sync = (Button) findViewById(R.id.sync);

        File dir = new File(path);
        dir.mkdirs();
        File txt = new File(path+"/pendientes.txt");//pendientes.txt es para los archivos pendientes
        File txt2 = new File(path+"/subidos.txt");//subidos.txt es para los registros subidos exitosamente

        if(!txt.exists()|| !txt2.exists()){
            try {
                if(!txt.exists()){
                    txt.createNewFile();
                } else {
                    txt2.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Save(txt2, "", false);//Cada vez que inicie la aplicacion truncar el de false
    }


    public void buttonSave (View view)
    {
        File file = new File (path + "/pendientes.txt");
        //String [] saveText = String.valueOf(editText.getText()).split(System.getProperty("line.separator"));
        if(nombres.getText().toString().equals("") || telefono.getText().toString().equals("") || email.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), "Imposible guardar registro. Datos incompletos!", Toast.LENGTH_SHORT).show();
        } else{
            String saveText = String.valueOf(nombres.getText()) + "," + String.valueOf(telefono.getText()) + "," + String.valueOf(email.getText()) + "\r\n";
            nombres.setText("");
            email.setText("");
            telefono.setText("");
            Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();

            Save (file, saveText,true);
        }

    }
    public void buttonTruncate (View view)
    {
        File file = new File (path + "/pendientes.txt");
        //String [] saveText = String.valueOf(editText.getText()).split(System.getProperty("line.separator"));
        //String saveText = String.valueOf("");
        /*
        nombres.setText("");
        email.setText("");
        telefono.setText("@string/telefono");*/

        Toast.makeText(getApplicationContext(), "Txt Truncated", Toast.LENGTH_LONG).show();

        Save (file, "",false);

    }

    public void buttonLoadSub (View view) //Este método de evento Load carga todo el txt
    {
        File file = new File (path + "/subidos.txt");
        String [] loadText = Load(file);
        //Toast.makeText(getApplicationContext(), String.valueOf(Load(file)), Toast.LENGTH_LONG);//Para ver cantidad de líneas
        String finalString = "";

        for (int i = 0; i < loadText.length; i++)
        {
            finalString += loadText[i] + System.getProperty("line.separator");
        }
        //textView.setText(String.valueOf(Load(file)));
        textView.setText(finalString);

    }
    public void buttonLoadPend (View view) /*Este método de evento Load carga todo el txt*/
    {
        File file = new File (path + "/pendientes.txt");
        String [] loadText = Load(file);
        //Toast.makeText(getApplicationContext(), String.valueOf(Load(file)), Toast.LENGTH_LONG);//Para ver cantidad de líneas
        String finalString = "";

        for (int i = 0; i < loadText.length; i++)
        {
            finalString += loadText[i] + System.getProperty("line.separator");
        }
        //textView.setText(String.valueOf(Load(file)));
        textView.setText(finalString);

    }
    private boolean haveNetworkConnection() {//Método para verificar disponibilidad de conexión
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
    //Debería haber un proceso background para el chequeo constante, además del manual, en donde cuando se quiera usar un txt se detenga el proceso
    //pero que cuando se termine su uso continue el proceso
    /*
    public boolean enviarRegistroDB(String[] registroBD){
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("nombre", registroBD[0]));
        nameValuePairs.add(new BasicNameValuePair("telefono", registroBD[1]));
        nameValuePairs.add(new BasicNameValuePair("email", registroBD[2]));

        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(
                    "http://10.0.2.2/cursoPHP/ingreso.php");
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = httpClient.execute(httpPost);

            HttpEntity entity = response.getEntity();
            return true;

        } catch (ClientProtocolException e) {
            return false;
        } catch (IOException e) {
            return false;
        }

    }*/


  //Metodo para ejecutar el AsynTask
   /* private void insertToDatabase(String nombre, String telefono, String email){

        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

                String paramNombre = params[0]; //name value
                String paramTelefono = params[1]; //cellphone value
                String paramEmail = params[2];//email value

                /*String name = editTextName.getText().toString();
                String add = editTextAdd.getText().toString();

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("nombre", paramNombre));
                nameValuePairs.add(new BasicNameValuePair("telefono", paramTelefono));
                nameValuePairs.add(new BasicNameValuePair("email", paramEmail));

                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(
                            "http://10.0.2.2/cursoPHP/ingreso.php");
                    HttpPost httpPost = new HttpPost(
                            "http://www.kairias97.net84.net/ingreso.php"); //In this site I have the php that inserts the registry within my db
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpClient.execute(httpPost);

                    HttpEntity entity = response.getEntity();

                    } catch (ClientProtocolException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                return "success";
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                    TextView textViewResult = (TextView) findViewById(R.id.textView);
                    textViewResult.setText("Inserted");


            }
        }
        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
        sendPostReqAsyncTask.execute(nombre, telefono, email);

    }*/
    /*public  boolean ingresarRegistro(){
        boolean hayConex = haveNetworkConnection();
        if(hayConex){
               return true;
        } else{
            return false;
        }
    }*/
  private void insertToDatabase(String pathSubida, String pathPendiente){
      class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
          File subidaTxt;
          File pendienteTxt;
          int ingresados;
          Exception error;
          SendPostReqAsyncTask(String pS, String pP){
              subidaTxt = new File(pS);
              pendienteTxt = new File(pP);
              ingresados = 0;
              error = null;
          }
          @Override
          protected String doInBackground(String... params) {
              int firstLine = 0;
              String[] lineas = Load(pendienteTxt);
              boolean b = lineas.length==0;
              while(firstLine < lineas.length && !b){
                  error=null;
                  String[] primera_linea = lineas[firstLine].split(",");
                  //Se prepara los pares de valores a enviar
                  List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                  nameValuePairs.add(new BasicNameValuePair("nombre", primera_linea[0]));
                  nameValuePairs.add(new BasicNameValuePair("telefono", primera_linea[1]));
                  nameValuePairs.add(new BasicNameValuePair("email", primera_linea[2]));
                  //Toast.makeText( getApplicationContext(), String.valueOf(primera_linea[0] + "," + primera_linea[1] +","+ primera_linea[2]), Toast.LENGTH_SHORT).show();
                  try {
                      HttpClient httpClient = new DefaultHttpClient();
                      HttpPost httpPost = new HttpPost(
                              "http://www.kairias97.net84.net/ingreso.php");
                      httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                      HttpResponse response = httpClient.execute(httpPost);

                      HttpEntity entity = response.getEntity();
                      //Para probar guardar en la bd y luego actualizar txt
                      Save(subidaTxt, lineas[firstLine], true);//añadir primer valor a subidos.txt
                      Save(pendienteTxt, "", false); //Trunco el txt
                      for(int i = firstLine+1; i < lineas.length; i++){
                          Save(subidaTxt, lineas[i],true);
                      }
                      firstLine++;
                      ingresados++;

                  } catch (ClientProtocolException e) {
                      error=e;
                  } catch (IOException e) {
                    error=e;
                  }
              }

              return "success";
          }

          @Override
          protected void onPostExecute(String result) {
              super.onPostExecute(result);
              Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
              if(ingresados==0){
                  Toast.makeText(getApplicationContext(), "No se añadieron nuevos registros!", Toast.LENGTH_SHORT).show();
              } else {
                  Toast.makeText(getApplicationContext(), "Se subieron correctamente "+ String.valueOf(ingresados) +" registros!", Toast.LENGTH_LONG).show();
              }
              //TextView textViewResult = (TextView) findViewById(R.id.textViewResult);
              //textViewResult.setText("Se subieron correctamente "+ String.valueOf(ingresados) +" registros!" );

          }
      }
      SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask(pathSubida, pathPendiente);
      sendPostReqAsyncTask.execute();
  }
    public void buttonSync(View view){
        //Verificacion de conexion
        boolean hayConex = haveNetworkConnection();
        //Verificacion de conexion para sincronizar txt
        if(hayConex){
            Toast.makeText(getApplicationContext(), "Hay conexion!!", Toast.LENGTH_LONG).show();
            insertToDatabase(path+"/subidos.txt", path+"/pendientes.txt");
            /*
            File pendientes = new File(path + "/pendientes.txt");
            File subidos = new File(path + "/subidos.txt");
            int firstLine = 0;
            String[] lineas = Load(pendientes);
            while(firstLine < lineas.length){
                String[] primera_linea = lineas[firstLine].split(",");
                //Toast.makeText( getApplicationContext(), String.valueOf(primera_linea[0] + "," + primera_linea[1] +","+ primera_linea[2]), Toast.LENGTH_SHORT).show();
                Save(subidos, lineas[firstLine], true);//añadir primer valor a subidos.txt
                Save(pendientes, "", false); //Trunco el txt
                for(int i = firstLine+1; i < lineas.length; i++){
                    Save(pendientes, lineas[i],true);
                }
                firstLine++;

            }*/
            //Toast.makeText(getApplicationContext(), "Exito", Toast.LENGTH_SHORT).show();


        } else{
            Toast.makeText(getApplicationContext(), "Noy hay conexion!!", Toast.LENGTH_LONG).show();
        }

        //Toast.makeText(getApplicationContext(), "Se han actualizado "+ contador + " registros", Toast.LENGTH_SHORT).show();
    }
    public static void Save(File file, String data, boolean typeWrite)//Tipo de escritura, update o truncate
    {
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(file,typeWrite);
        }
        catch (FileNotFoundException e) {e.printStackTrace();}
        try
        {
            try
            {
                //data.concat(System.getProperty("line.separator"));
                fos.write(data.getBytes());

                /*
                for (int i = 0; i<data.length; i++)
                {
                    fos.write(data[i].getBytes());

                    if (i < data.length-1)
                    {
                        fos.write("\n".getBytes());
                    }
                }*/

            }
            catch (IOException e) {e.printStackTrace();}
        }
        finally
        {
            try
            {
                fos.close();
            }
            catch (IOException e) {e.printStackTrace();}
        }
    }


    public static String[] Load(File file)
    {
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(file);
        }
        catch (FileNotFoundException e) {e.printStackTrace();}
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        String test;
        //Captura la cantidad de líneas del documento
        int anzahl=0;
        try
        {
            while ((test=br.readLine()) != null)
            {
                anzahl++;
            }
        }
        catch (IOException e) {e.printStackTrace();}

        try
        {
            fis.getChannel().position(0);
        }
        catch (IOException e) {e.printStackTrace();}

        String[] array = new String[anzahl];//Cadena de strings con cada linea

        String line;
        int i = 0;
        try
        {
            while((line=br.readLine())!=null)
            {
                array[i] = line; //En el array de tamaño n líneas de txt
                i++;
            }
        }
        catch (IOException e) {e.printStackTrace();}
        return array;
        //return anzahl;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
