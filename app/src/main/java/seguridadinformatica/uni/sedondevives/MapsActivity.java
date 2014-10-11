package seguridadinformatica.uni.sedondevives;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;



public class MapsActivity extends FragmentActivity   implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationClient myLocationClient;
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    private GoogleMap googleMap; // Might be null if Google Play services APK is not available.
    private Location myLocation = null;


    public void onClickEnviar(View view) {
        if(myLocation == null)
        {
            mostrar("ubicacion actual no disponible");
            return;
        }
        String data= Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID)+
                '?'+Double.toString(myLocation.getLatitude())+
                '?'+Double.toString(myLocation.getLongitude());
        data=encrypt(data);
        new EnviarLocacion().execute(data);
    }

    class EnviarLocacion extends AsyncTask<String, Void, String > {
        Exception ex;
        protected String doInBackground(String... datos) {
            String  result= null;
            AndroidHttpClient client=null;
            try {

                String url="http://pruebasseguridad.t15.org/saveMyLocation.php";
                HttpResponse response;
                HttpPost request;
                client = AndroidHttpClient.newInstance("somename");

                request = new HttpPost(url);

                // Request parameters and other properties.
                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                params.add(new BasicNameValuePair("data", datos[0]));
                request.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));


                response = client.execute(request);
                InputStream source = response.getEntity().getContent();
                BufferedReader br= new BufferedReader(new InputStreamReader(source));
                String linea;
                result = "";
                while( (linea= br.readLine() ) != null )
                {
                    result+=linea+'\n';
                }
                if(client != null) client.close();

            } catch (Exception e) {
                this.ex= e;
                if(client != null)client.close();
            }
            return result;
        }

        protected void onPostExecute(String v) {
            if(ex != null) Log.d(this.toString(),ex.toString());
            if(v!=null) mostrar(v);
            else mostrar("Conexion al servidor fallida");
        }
    }

    public void mostrar(String s)
    {
        Toast.makeText(this,s,Toast.LENGTH_LONG).show();
    }

    private String encrypt(String data) {
        ApiCrypter ap= new ApiCrypter();
        try {
            return ApiCrypter.bytesToHex(ap.encrypt(data));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

         /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        myLocationClient = new LocationClient(this, this, this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #googleMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (googleMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (googleMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #googleMap} is not null.
     */
    private void setUpMap() {
        googleMap.setMapType( GoogleMap.MAP_TYPE_HYBRID );
        googleMap.setMyLocationEnabled(true);
        googleMap.setPadding(0,80,0,0); //sets space for search buttom and editText box

        //googleMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }


    @Override
    protected void onStart() {
        super.onStart();
        if(myLocationClient != null)
            myLocationClient.connect();
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        myLocationClient.disconnect();
        super.onStop();
    }
    @Override
    public void onConnected(Bundle bundle) {
        // Display the connection status
        mostrar("conectado");
        myLocationClient.requestLocationUpdates( REQUEST, this);
        //no hay tiempo suficiente para obtener la locacion en este instante
        /*

        GoogleMap myMap = ((SupportMapFragment) getSupportFragmentManager (). findFragmentById (R.id.map))
                . getMap();

        LatLng coor= new LatLng(myLocationClient.getLastLocation().getLatitude(),myLocationClient.getLastLocation().getLongitude());

        CameraUpdate update =  CameraUpdateFactory.newLatLngZoom(coor,15);

        myMap.animateCamera(update);
        */


    }

    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        myLocation = location;
        //mostrar("location changed");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            showDialog(connectionResult.getErrorCode());
        }
    }


    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates",
                    "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Get the error code
            int errorCode =  resultCode;//ConnectionResult.getErrorCode();
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    errorCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                // Set the dialog in the DialogFragment

                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                //errorFragment.show(getSupportFragmentManager(),"Location Updates");
            }
            return false;
        }
    }



    /*
     * Handle results returned to the FragmentActivity
     * by Google Play services
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {

            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                switch (resultCode) {
                    case Activity.RESULT_OK :
                        Toast.makeText(this,"Try to connect again",Toast.LENGTH_LONG).show();
                    /*
                     * Try the request again
                     */

                        break;
                }

        }
    }


    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

}
