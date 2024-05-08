package com.example.myapplication.game4

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.game1.Game1
import com.example.myapplication.game1.MultiPlayers
import java.io.IOException
import java.util.UUID

class Multiplayer4 : AppCompatActivity(), AdapterView.OnItemClickListener, SensorEventListener {
    private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FD")
    private lateinit var oursocket: BluetoothSocket
    private val REQUEST_ENABLE_BT = 1
    private lateinit var bluetoothAdapter: BluetoothAdapter
    lateinit var bluetoothManager: BluetoothManager
    private lateinit var deviceListAdapter: ArrayAdapter<String>
    private var deviceList = ArrayList<String>()
    private var deviceListAdr = ArrayList<String>()

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var lastUpdate: Long = 0
    private var shakeCount = 0
    private var shakeCountServer = 0
    private var shakeCountClient = 0
    private var isServer = false
    private var isClient = false

    private lateinit var scoreTextView: TextView
    private lateinit var scoreTextViewADV: TextView
    private  var lastX = 0f
    private  var lastY = 0f
    private  var lastZ = 0f
    private lateinit var countDownTimer: CountDownTimer
    private var finish = false

    lateinit var mediaPlayer: MediaPlayer

    lateinit var mediaPlayer_suc: MediaPlayer
    lateinit var mediaPlayer_fail: MediaPlayer

    companion object {
        private const val SHAKE_THRESHOLD = 500
        private const val SHAKE_INTERVAL = 500
        private const val NAME = "YourBluetoothServerName"
        private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //setContentView(R.layout.popup_invite_bluetooth)
        setContentView(R.layout.multiplayer4)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.multiplayer4)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        scoreTextView = findViewById(R.id.scoreTextView)
        scoreTextViewADV = findViewById(R.id.scoreTextViewADV)

        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        lastUpdate = System.currentTimeMillis()

        mediaPlayer = MediaPlayer()

        // Initialize MediaPlayer
        mediaPlayer_suc = MediaPlayer.create(this, R.raw.succes)
        mediaPlayer_fail = MediaPlayer.create(this, R.raw.fail)

        // Register for broadcasts when a device is discovered.
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)

        val quitButton = findViewById<Button>(R.id.quit)

        quitButton.setOnClickListener{
            val intent = Intent(applicationContext, Game4::class.java)
            startActivity(intent)
            finish()
        }

        showBLEconnect()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // Le Bluetooth a été activé avec succès
                //Toast.makeText(this, "Bluetooth activé avec succès", Toast.LENGTH_SHORT).show()
                // Ajoutez ici le code à exécuter lorsque le Bluetooth est activé
                searchBluetoothDevices()

            } else {
                // L'utilisateur a annulé ou refusé d'activer le Bluetooth
                Toast.makeText(this, "Activation du Bluetooth annulée", Toast.LENGTH_SHORT).show()
                // Ajoutez ici le code à exécuter lorsque l'activation du Bluetooth est annulée
                val intent = Intent(applicationContext, Game1::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun searchBluetoothDevices() {
        deviceList.clear()
        deviceListAdapter.notifyDataSetChanged()

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices

        pairedDevices?.forEach { device ->
            deviceList.add("${device.name}\n${device.address}")
            deviceListAdr.add(device.address)
        }

        // Démarrez la découverte de nouveaux périphériques
        bluetoothAdapter.startDiscovery()
    }

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device?.name
                    val deviceHardwareAddress = device?.address // MAC address

                    deviceList.add("${deviceName}\n${device?.address}")
                    if (deviceHardwareAddress != null) {
                        deviceListAdr.add(deviceHardwareAddress)
                    }

                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun showBLEconnect() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.popup_invite_bluetooth, null)
        //val titleTextView = dialogView.findViewById<TextView>(R.id.text_title)
        //val messageTextView = dialogView.findViewById<TextView>(R.id.text_message)
        val inviteButton = dialogView.findViewById<Button>(R.id.invite)
        val waitButton = dialogView.findViewById<Button>(R.id.attente)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        deviceList = ArrayList()
        deviceListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceList)

        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        inviteButton.setOnClickListener {// on recherche un server, c'est le client
            val listView = findViewById<ListView>(R.id.deviceListView)
            listView.visibility = View.VISIBLE
            listView.adapter = deviceListAdapter
            listView.onItemClickListener = this

            if (bluetoothAdapter == null){
                //Toast.makeText(this, "Impossible d'utiliser le bluetooth", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Connection est possible", Toast.LENGTH_SHORT).show()
                //val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
                bluetoothAdapter = bluetoothManager.adapter
                if (bluetoothAdapter == null){
                    Toast.makeText(this, "Impossible d'utiliser le bluetooth", Toast.LENGTH_SHORT).show()
                } else {
                    if(!bluetoothAdapter.isEnabled){
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        if (ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            val REQUEST_CODE_BLUETOOTH_PERMISSION = 1001
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                                REQUEST_CODE_BLUETOOTH_PERMISSION
                            )
                        }
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                        searchBluetoothDevices()
                    }else{ //Si le Bluetooth est déja activé
                        searchBluetoothDevices()
                    }
                }
            }
            dialog.dismiss()
        }

        //on attend une connexion (c'est le serveur)
        /*waitButton.setOnClickListener {
            val progress = dialogView.findViewById<View>(R.id.progressBar)
            progress.visibility = View.VISIBLE
            //Toast.makeText(this, "En attente d'un ami", Toast.LENGTH_SHORT).show()
            val accepted = AcceptThread(dialog)
            accepted.start()
            //dialog.dismiss()
        }*/

        waitButton.setOnClickListener {
            val progress = dialogView.findViewById<View>(R.id.progressBar)
            progress.visibility = View.VISIBLE

            if (bluetoothAdapter == null){
                //Toast.makeText(this, "Impossible d'utiliser le bluetooth", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this, "Connection est possible", Toast.LENGTH_SHORT).show()
                // val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
                bluetoothAdapter = bluetoothManager.adapter
                if (bluetoothAdapter == null){
                    Toast.makeText(this, "Impossible d'utiliser le bluetooth", Toast.LENGTH_SHORT).show()
                } else {
                    if(!bluetoothAdapter.isEnabled){
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        if (ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            val REQUEST_CODE_BLUETOOTH_PERMISSION = 1001
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                                REQUEST_CODE_BLUETOOTH_PERMISSION
                            )
                        }


                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                        //searchBluetoothDevices()
                        val accepted = AcceptThread(dialog)
                        accepted.start()
                    }else{ //Si le Bluetooth est déja activé
                        //searchBluetoothDevices()
                        val accepted = AcceptThread(dialog)
                        accepted.start()
                    }
                }
            }
            //Toast.makeText(this, "En attente d'un ami", Toast.LENGTH_SHORT).show()
            //val accepted = AcceptThread(dialog)
            //accepted.start()
            //dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Arrêtez la découverte de périphériques lorsque l'activité est détruite pour éviter les fuites de mémoire
        bluetoothAdapter.cancelDiscovery()
        oursocket.close()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        println("Click")
        // Récupérez le périphérique correspondant à l'élément cliqué
        val selectedDevice = deviceListAdr[position]

        println(selectedDevice)
        // Créez une connexion avec le périphérique sélectionné
        val device: BluetoothDevice? = bluetoothAdapter.getRemoteDevice(selectedDevice)
        println(device)

        val socket= device?.createRfcommSocketToServiceRecord(MY_UUID)

        // Lancez la tentative de connexion dans un thread séparé
        val connectThread = ConnectThread(socket)
        println("Click")
        if (socket != null) {
            oursocket = socket
        }
        val scrollbar = findViewById<View>(R.id.progressBar3)
        scrollbar.visibility = View.VISIBLE
        connectThread.start()

        //println("Start client")
        //println(selectedDevice)

        // En fonction de selectedDevice, envoyez une demande de connexion
        // Utilisez la méthode pour envoyer une demande de connexion
        // par exemple : sendConnectionRequest(selectedDevice)
        // sendConnectionRequest(selectedDevice)
    }

    @SuppressLint("MissingPermission")
    private inner class AcceptThread(private val dialog: AlertDialog?) : Thread() {

        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(MultiPlayers.NAME, MY_UUID)
        }

        override fun run() {
            // Keep listening until exception occurs or a socket is returned.
            println("Demarrage du serveur")
            isServer = true
            var shouldLoop = true
            //var socket: BluetoothSocket?
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    mmServerSocket?.accept()
                }

                catch (e: IOException) {
                    Log.e(ContentValues.TAG, "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }
                socket?.also {
                    oursocket = socket
                    manageCommunicationServer(it)
                    //mmServerSocket?.close()
                    //shouldLoop = false
                }
            }
        }

        fun sendMessage( message: String){
            try {
                val outputStream = oursocket.outputStream
                outputStream?.write(message.toByteArray())
            } catch (e: IOException) {
                // Gérer l'exception
                println(e)
            }
        }

        fun manageCommunicationServer(socket: BluetoothSocket) {
            try {
                while(true){
                    // Lire les données reçues du périphérique connecté
                    val inputStream = socket.inputStream
                    val availableBytes = inputStream.available()

                    if (availableBytes > 0) {
                        val buffer = ByteArray(1024)
                        val bytesRead = inputStream.read(buffer)
                        val receivedMessage = String(buffer, 0, bytesRead)
                        Log.d(ContentValues.TAG, "Message reçu : $receivedMessage")

                        val outputStream = socket.outputStream

                        when (receivedMessage) {
                            "Start Game" -> {
                                println("Game is starting")
                                dialog?.dismiss()
                                startGame()
                                val response = "Ok_Start_Game"
                                outputStream.write(response.toByteArray())
                                Log.d(ContentValues.TAG, "Reponse serveur : $response")
                                //println("cc")

                            }
                            "2" -> {
                                println("x is 2")
                            }
                            "3" -> {
                                println("x is 3")
                            }
                            else -> println(receivedMessage)
                        }
                        if(receivedMessage.startsWith("ADV")){
                            val scoreAdvTextView = findViewById<TextView>(R.id.scoreTextViewADV)
                            shakeCountClient= receivedMessage.substringAfter("ADV : ").toInt()
                            runOnUiThread{
                                scoreAdvTextView.text = "ADV : $shakeCountClient"
                            }
                            //scoreAdvTextView.text = "ADV : $shakeCountClient"
                        }
                    }
                }


                // Envoyer une réponse au périphérique connecté



                // Fermer le socket après avoir terminé la communication
                //println("Fin de communication")
                //socket.close()
            } catch (e: IOException) {
                Log.e(ContentValues.TAG, "Erreur lors de la gestion de la connexion", e)
            }
        }

        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            try {
                mmServerSocket?.close()
            } catch (e: IOException) {
                Log.e(ContentValues.TAG, "Could not close the connect socket", e)
            }
        }

    }

    private inner class ConnectThread(private val socket: BluetoothSocket?) : Thread() {

        override fun run() {
            println("We are here")
            try {
                // Tentez de connecter le socket
                socket?.connect()
                val listView = findViewById<ListView>(R.id.deviceListView)
                listView.visibility = View.INVISIBLE

                val scrollbar = findViewById<View>(R.id.progressBar3)
                scrollbar.visibility = View.INVISIBLE

                // La connexion est établie avec succès, vous pouvez maintenant communiquer avec le périphérique
                // Par exemple, envoyer et recevoir des données
                println("Succes de la connexion")
                isClient = true

                //Envoyer un message
                /*val outputStream = socket?.outputStream

                // Écrire le message à envoyer sur le flux de sortie
                val message = "Connecté"
                outputStream?.write(message.toByteArray())
                println("Succes de la connexion")*/
                sendMessage("Start Game")
                while (true){
                    listenForMessages() // foctionne un message
                }

            } catch (e: IOException) {
                // Une exception s'est produite lors de la tentative de connexion
                // Gérer cette situation en conséquence
                println(e)
            }
        }

        fun sendMessage(message: String) {
            try {
                val outputStream = socket?.outputStream
                outputStream?.write(message.toByteArray())
            } catch (e: IOException) {
                // Gérer l'exception
                println(e)
            }
        }

        // Méthode pour écouter en continu pour les nouveaux messages
        private fun listenForMessages() {
            try {
                val inputStream = socket?.inputStream
                val availableBytes = inputStream?.available()

                if (availableBytes != null) {
                    if (availableBytes > 0) {
                        val buffer = ByteArray(1024)
                        var bytes: Int

                        // Boucle pour lire continuellement à partir du flux d'entrée
                        while (true) {
                            bytes = inputStream.read(buffer) ?: break
                            val message = String(buffer, 0, bytes)

                            // Traiter le message reçu
                            println("Message reçu: $message")
                            if (message == "Ok_Start_Game"){
                                startGame()
                            }
                            if(message.startsWith("ADV : ")){
                                val scoreAdvTextView = findViewById<TextView>(R.id.scoreTextViewADV)
                                shakeCountServer= message.substringAfter("ADV : ").toInt()
                                runOnUiThread {
                                    scoreAdvTextView.text = "ADV : ${shakeCountServer}"
                                }

                                println("mise a jour ADV")
                            }

                        }
                    }
                }
            } catch (e: IOException) {
                // Gérer l'exception
                println(e)
            }
        }

        // Méthode pour annuler la connexion
        fun cancel() {
            try {
                socket?.close()
            } catch (e: IOException) {
                // Gérer l'exception
            }
        }
    }

    private fun startGame(){
        Handler(Looper.getMainLooper()).post{
            startCountDownTimer()
        }
    }

    private fun startCountDownTimer() {
        val timerTextView = findViewById<TextView>(R.id.timer)

        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Met à jour le texte du TextView avec le temps restant
                val secondsLeft = millisUntilFinished / 1000
                timerTextView.text = "Temps restant : $secondsLeft s"
            }

            override fun onFinish() {
                showGameOverDialog()
            }
        }
        // Démarre le compte à rebours
        countDownTimer.start()
    }

    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val currentTime = System.currentTimeMillis()
            if ((currentTime - lastUpdate) > SHAKE_INTERVAL) {
                val diffTime = currentTime - lastUpdate
                lastUpdate = currentTime

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000
                if (speed > SHAKE_THRESHOLD && !finish) {
                    shakeCount++
                    scoreTextView.text = "Mon Score: $shakeCount"
                    if(isServer){
                        shakeCountServer = shakeCount
                        AcceptThread(null).sendMessage("ADV : $shakeCountServer")
                    }
                    if(isClient){
                        shakeCountClient = shakeCount
                        ConnectThread(oursocket).sendMessage("ADV : $shakeCountClient")
                    }
                }
                lastX = x
                lastY = y
                lastZ = z
            }
        }
    }

    private fun showGameOverDialog() {
        finish = true
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_game_over, null)
        val titleTextView = dialogView.findViewById<TextView>(R.id.text_title)
        val messageTextView = dialogView.findViewById<TextView>(R.id.text_message)
        val quitButton = dialogView.findViewById<Button>(R.id.button_quit)
        val resumeButton = dialogView.findViewById<Button>(R.id.button_resume)

        if (isServer && shakeCountServer>shakeCountClient){
            titleTextView.text = "😏 Victoire 😎"
            mediaPlayer_suc.start()
        }else if(isServer && shakeCountServer<shakeCountClient){
            titleTextView.text = "😪 Défaite 😭"
            mediaPlayer_fail.start()
        }
        if (isClient && shakeCountServer<shakeCountClient){
            titleTextView.text = "😏 Victoire 😎"
            mediaPlayer_suc.start()
        }else if(isClient && shakeCountServer>shakeCountClient){
            titleTextView.text = "😪 Défaite 😭"
            mediaPlayer_fail.start()
        }

        //titleTextView.text = "Fin de la partie"
        messageTextView.text = "Votre Score: $shakeCount"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        quitButton.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(applicationContext, Game4::class.java)
            startActivity(intent)
            finish()
        }

        resumeButton.setOnClickListener {
            dialog.dismiss()
            startCountDownTimer()
            val scoreTextView = findViewById<TextView>(R.id.scoreTextView)
            finish=false
            shakeCount = 0
            scoreTextView.text = "Score $shakeCount"
        }

        dialog.show()
    }
}