package com.example.myapplication.game3

import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Point
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.example.myapplication.game1.Game1
import java.io.IOException
import java.util.Random
import java.util.UUID
import kotlin.math.abs


class MultiPlayer3 : AppCompatActivity(), AdapterView.OnItemClickListener, SensorEventListener {
    private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FC")
    private  var Ownsocket: BluetoothSocket? = null
    private val REQUEST_ENABLE_BT = 1
    private lateinit var bluetoothAdapter: BluetoothAdapter
    lateinit var bluetoothManager: BluetoothManager
    private lateinit var deviceListAdapter: ArrayAdapter<String>
    private var deviceList = ArrayList<String>()
    private var deviceListAdr = ArrayList<String>()

    private var finish = false

    lateinit var timerTextView: TextView
    private lateinit var countDownTimer: CountDownTimer
    private var horizontalBias: Float = 0.547f
    private var score: Int = 0
    private var currentImagePosition: Float = 0f
    private lateinit var oursocket: BluetoothSocket

    private var isServer = false
    private var serveurLife: Int = 5
    private var isClient = false
    private var clientlife: Int = 5

    companion object {
        const val NAME = "YourBluetoothServerName"
        private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private lateinit var chat: ImageView
    private var threshold = 1
    private var defilement = 0f
    private var duration = 3000L

    private var collisionLeft = false
    private var collisionCenter = false
    private var collisionRight = false
    private lateinit var gestureDetector: GestureDetector

    private lateinit var fireViews: List<ImageView>
    private val fireAnimations = mutableListOf<Animator>()
    private val random = Random()
    private var delayList = arrayOf(500L, 1200L, 1900L, 500L)
    var arrayDuration = arrayOf(2000L,2500L, 3000L,3500L, 4000L)
    private var life = 5
    val handler = Handler() //pour augmenter le score, on l'utilise aussi pour redemaree le score
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lastMovementTime = 0L
    private val movementDelay = 500L

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //setContentView(R.layout.popup_invite_bluetooth)
        setContentView(R.layout.multiplayer3)

        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val screenWidth = size.x
        chat = findViewById(R.id.chat)
        fireViews = listOf(
            findViewById(R.id.fire),
            findViewById(R.id.fire2),
            findViewById(R.id.fire3),
            // Ajoutez les autres ImageView des boules de feu ici
        )

        // Calculez le seuil comme un tiers de la taille de l'écran
        defilement = (screenWidth / 3).toFloat()

        chat = findViewById(R.id.chat)
        gestureDetector = GestureDetector(this, GestureListener())

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.multiplayer3)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)


        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        //val imageView: ImageView = findViewById(R.id.maflechette)

        /*imageView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Action lorsque l'utilisateur commence à toucher l'image
                    true
                }
                MotionEvent.ACTION_MOVE -> {

                    // Calculer le déplacement horizontal
                    val deltaX = event.rawX - view.width / 2 - view.x
                    val parentLayout = findViewById<ConstraintLayout>(R.id.multiplayer)
                    val parentWidth = parentLayout.width.toFloat()

                    //val imageView = findViewById<ImageView>(R.id.mafleche)
                    val halfImageWidth = imageView.width / 2
                    horizontalBias =(view.x + halfImageWidth) / parentWidth

                    // Mettre à jour la position horizontale de l'image
                    view.x += deltaX

                    true
                }
                MotionEvent.ACTION_UP -> {
                    // Action lorsque l'utilisateur arrête de toucher l'image
                    true
                }
                else -> false
            }
        }*/

        // Register for broadcasts when a device is discovered.
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)

        showBLEconnect()


    }

    /*override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }*/

    private inner class GestureListener: GestureDetector.SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            //val deltaX = e2.x - e1.x
            val deltaX = (e2.x - e1?.x!!) ?: 0f
            if (abs(deltaX) > threshold) {
                if (deltaX > 0) {
                    moveImageRight()
                } else {
                    moveImageLeft()
                }
                return true
            }

            return false
        }

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }
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
                val intent = Intent(applicationContext, Game3::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor == accelerometer) {
            val xAcc = event.values[0]
            val yAcc = event.values[1]

            val currentTime = System.currentTimeMillis()

            // Vérifiez si le délai entre les mouvements est écoulé
            if (currentTime - lastMovementTime >= movementDelay) {
                if (abs(xAcc) > abs(yAcc)) {
                    if (xAcc < -3.0) {
                        moveImageRight()
                        lastMovementTime = currentTime // Mettez à jour le temps du dernier mouvement
                    } else if (xAcc > 3.0) {
                        moveImageLeft()
                        lastMovementTime = currentTime // Mettez à jour le temps du dernier mouvement
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Ignored
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

            } else {
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
        waitButton.setOnClickListener {
            val progress = dialogView.findViewById<View>(R.id.progressBar)
            progress.visibility = View.VISIBLE

            if (bluetoothAdapter == null){
                //Toast.makeText(this, "Impossible d'utiliser le bluetooth", Toast.LENGTH_SHORT).show()

            } else {
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
        //sendConnectionRequest(selectedDevice)
    }

    @SuppressLint("MissingPermission")
    private inner class AcceptThread(private val dialog: AlertDialog?) : Thread() {

        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(NAME, MY_UUID)
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
                    Log.e(TAG, "Socket's accept() method failed", e)
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
                        Log.d(TAG, "Message reçu : $receivedMessage")

                        val outputStream = socket.outputStream

                        when (receivedMessage) {
                            "Start Game" -> {
                                println("Game is starting")
                                dialog?.dismiss()
                                startGame()
                                val response = "Ok_Start_Game"
                                outputStream.write(response.toByteArray())
                                Log.d(TAG, "Reponse serveur : $response")
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
                            val scoreAdvTextView = findViewById<TextView>(R.id.lifeADV)
                            clientlife= receivedMessage.substringAfter("ADV : ").toInt()
                            if (clientlife == 0){
                                runOnUiThread {
                                    showGameOverDialog() // Appeler votre méthode pour afficher la boîte de dialogue ici
                                }
                            }
                            scoreAdvTextView.text = "ADV : $clientlife"
                        }
                    }
                }

                // Envoyer une réponse au périphérique connecté

                // Fermer le socket après avoir terminé la communication
                //println("Fin de communication")
                //socket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Erreur lors de la gestion de la connexion", e)
            }
        }

        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            try {
                mmServerSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
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
                                val scoreAdvTextView = findViewById<TextView>(R.id.lifeADV)
                                serveurLife= message.substringAfter("ADV : ").toInt()
                                if (serveurLife == 0){

                                    runOnUiThread {
                                        showGameOverDialog() // Appeler votre méthode pour afficher la boîte de dialogue ici
                                    }
                                }
                                scoreAdvTextView.text = "ADV : ${serveurLife}"
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

    private fun showGameOverDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.overonlinegame1, null)
        //val titleTextView = dialogView.findViewById<TextView>(R.id.text_title)
        val messageTextView = dialogView.findViewById<TextView>(R.id.result)
        val quitButton = dialogView.findViewById<Button>(R.id.quitter)
        val resumeButton = dialogView.findViewById<Button>(R.id.replay)

        //titleTextView.text = "Partie terminée"
        //messageTextView.text = "Que voulez-vous faire ?"

        println(isServer)
        println(isClient)
        println(serveurLife)
        println(clientlife)

        finish = true

        if (isServer && serveurLife>clientlife){
            messageTextView.text = "😏 Victoire 😎"
        }else if(isServer && serveurLife<clientlife){
            messageTextView.text = "😪 Défaite 😭"
        }
        if (isClient && serveurLife<clientlife){
            messageTextView.text = "😏 Victoire 😎"
        }else if(isClient && serveurLife>clientlife){
            messageTextView.text = "😪 Défaite 😭"
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        quitButton.setOnClickListener {
            dialog.dismiss()
            //Toast.makeText(this, "Bouton 6 cliqué", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, Game3::class.java)
            startActivity(intent)
            finish()
        }

        resumeButton.setOnClickListener {
            dialog.dismiss()
            //startCountDownTimer()

            val scoreTextView = findViewById<TextView>(R.id.lifeADV)
            //val scoreAdvTextView = findViewById<TextView>(R.id.scoreADV)
            finish = false
            life = 5
            clientlife = 5
            serveurLife = 5
            scoreTextView.text = "Vie ADV: $life"
            //scoreAdvTextView.text = "ADV : $score"
        }

        dialog.show()
    }

    private fun moveFire(){
        val chat = findViewById<ImageView>(R.id.chat)
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        val dpValue_fire = 100 // La valeur en dp que vous souhaitez convertir
        val density = resources.displayMetrics.density
        val fireHeight = (dpValue_fire * density + 0.5f).toInt()

        val initialY = -fireHeight.toFloat()
        val finalY = screenHeight.toFloat()

        var fireViews = listOf(
            findViewById<ImageView>(R.id.fire),
            findViewById<ImageView>(R.id.fire2),
            findViewById<ImageView>(R.id.fire3),
            // Ajoutez les autres ImageView des boules de feu ici
        )

        //fireViews = fireViews.shuffled()

        var animators = mutableListOf<Animator>()
        //var delai = arrayOf(500L, 1000L, 1500L, 500L)

        delayList.shuffle()

        fireViews.forEachIndexed { index, imageView ->
            val animator = ObjectAnimator.ofFloat(imageView, "translationY", initialY, finalY)
            animator.duration = duration // Durée de l'animation en millisecondes
            animator.repeatCount = ValueAnimator.RESTART
            animator.interpolator = LinearInterpolator()
            //delayList[0] = delai[index]

            animator.startDelay = delayList[index] // Définit le délai de démarrage de l'animation

            animator.addUpdateListener { animation ->
                val currentValue = animation.animatedValue as Float + fireHeight

                if (chat.y < currentValue && currentValue < chat.y + chat.height) {
                    // Collision détectée avec le chat
                    //println(chat.x)
                    if ( chat.x < screenWidth / 3 && !collisionLeft && index == 0) {
                        collisionLeft = true

                        if(!finish){
                            life -= 1
                            life()
                        }

                        imageView.translationY = initialY
                        imageView.visibility = View.INVISIBLE
                        animator.cancel()

                    }else if ( screenWidth / 3 < chat.x && chat.x < 2*screenWidth / 3 && !collisionCenter && index == 1) {
                        collisionCenter = true

                        if(!finish){
                            life -= 1
                            life()
                        }

                        imageView.translationY = initialY
                        imageView.visibility = View.INVISIBLE
                        animator.cancel()

                    }else if (chat.x > 2*screenWidth / 3 && !collisionRight && index == 2) {
                        collisionRight = true
                        if(!finish){
                            life -= 1
                            life()
                        }

                        imageView.translationY = initialY
                        imageView.visibility = View.INVISIBLE
                        animator.cancel()

                    }
                }
                if(currentValue > chat.y + chat.height){
                    collisionLeft = false
                    collisionRight = false
                    collisionCenter = false
                }
            }

            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    imageView.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animator) {
                    //println("Fin d'animation")
                    //collisionLeft = false
                    //imageView.visibility = View.VISIBLE
                    println("Rends visible merde")
                    val randomValue = (0..5).random()
                    //val randomIndex = delai.indices.random()
                    animation.setDuration(duration)
                    animation.startDelay = delayList[index]

                    animation.start()

                }

                override fun onAnimationCancel(animation: Animator) {
                    // Rien à faire ici
                    println("Collision detecté")
                    imageView.visibility = View.INVISIBLE

                    //animation.start()
                }

                override fun onAnimationRepeat(animation: Animator) {
                    // Rien à faire ici
                }
            })

            animators.add(animator)
        }
        //animators = animators.shuffled().toMutableList()

        animators.forEach { it.start() }

        //doSomethingEveryTwoSeconds(animators)
    }

    private fun moveImageRight() {
        val screenWidth = resources.displayMetrics.widthPixels
        if (!(chat.x >= 2*screenWidth/3)){
            chat.animate().translationXBy(defilement).setDuration(50).start()

        }
        //chat.animate().translationXBy(defilement).setDuration(100).start()
    }

    private fun moveImageLeft() {
        val screenWidth = resources.displayMetrics.widthPixels
        if (!(chat.x <= screenWidth/3)){
            chat.animate().translationXBy(-defilement).setDuration(50).start()
        }
    }

    private fun shuffleDelay(){
        val handler = Handler()
        val delayMillis = 500L // 0,5 secondes

        val runnable = object : Runnable {
            override fun run() {
                //on diminue les temps de defilement de
                delayList.shuffle()
                handler.postDelayed(this, delayMillis)
            }
        }

        // Planifier la première exécution dans 2 secondes
        handler.postDelayed(runnable, delayMillis)
    }

    private fun riseDifficulty() {
        val handler = Handler()
        val delayMillis = 10000L // 2 secondes

        val runnable = object : Runnable {
            override fun run() {
                //on diminue les temps de defilement de
                if(duration > 1000){
                    duration -= 300
                }
                //arrayDuration = arrayDuration.map { if (it > 1000) it - 100 else 1100 }.toTypedArray()
                // Planifier la prochaine exécution dans 1 secondes
                handler.postDelayed(this, delayMillis)
            }
        }

        // Planifier la première exécution dans 2 secondes
        handler.postDelayed(runnable, delayMillis)
    }

    /*private fun score() {
        val delayMillis = 500L // 2 secondes

        val runnable = object : Runnable {
            override fun run() {
                //on diminue les temps de defilement de
                println("Score")
                score += 3
                val scoreTextView = findViewById<TextView>(R.id.scoregame2)
                scoreTextView.text = "SCORE\n$score"
                // Planifier la prochaine exécution dans 1 secondes
                handler.postDelayed(this, delayMillis)
            }
        }

        // Planifier la première exécution dans 2 secondes
        handler.postDelayed(runnable, delayMillis)
    }*/

    private fun stopScoreTimer() {
        // Arrête l'exécution périodique de la fonction score()
        handler.removeCallbacksAndMessages(null)
    }

    private fun life(){
        if (life == 4 ){
            if (isClient) {
                clientlife = life
                println("le client envoie son score")
                ConnectThread(oursocket).sendMessage("ADV : $life")
            }
            if (isServer) {
                serveurLife = life
                println("le server envoie son score")
                AcceptThread(null).sendMessage("ADV : $life")
            }
            vibratePhone(this, 200)
            val heart = findViewById<ImageView>(R.id.heart5)
            heart.visibility = View.INVISIBLE

        }else if(life == 3){
            if (isClient) {
                clientlife = life
                println("le client envoie son score")
                ConnectThread(oursocket).sendMessage("ADV : $life")
            }
            if (isServer) {
                serveurLife = life
                println("le server envoie son score")
                AcceptThread(null).sendMessage("ADV : $life")
            }
            vibratePhone(this, 200)
            val heart = findViewById<ImageView>(R.id.heart4)
            heart.visibility = View.INVISIBLE

        }else if(life == 2){
            if (isClient) {
                clientlife = life
                println("le client envoie son score")
                ConnectThread(oursocket).sendMessage("ADV : $life")
            }
            if (isServer) {
                serveurLife = life
                println("le server envoie son score")
                AcceptThread(null).sendMessage("ADV : $life")
            }
            vibratePhone(this, 200)
            val heart = findViewById<ImageView>(R.id.heart3)
            heart.visibility = View.INVISIBLE

        }else if(life == 1){
            if (isClient) {
                clientlife = life
                println("le client envoie son score")
                ConnectThread(oursocket).sendMessage("ADV : $life")
            }
            if (isServer) {
                serveurLife = life
                println("le server envoie son score")
                AcceptThread(null).sendMessage("ADV : $life")
            }
            vibratePhone(this, 200)
            val heart = findViewById<ImageView>(R.id.heart2)
            heart.visibility = View.INVISIBLE

        }else if(life == 0){
            finish = true
            if (isClient) {
                clientlife = life
                println("le client envoie son score")
                ConnectThread(oursocket).sendMessage("ADV : $life")
            }
            if (isServer) {
                serveurLife = life
                println("le server envoie son score")
                AcceptThread(null).sendMessage("ADV : $life")
            }
            vibratePhone(this, 200)
            val heart = findViewById<ImageView>(R.id.heart1)
            heart.visibility = View.INVISIBLE

            showGameOverDialog()


            /*val dialogView = LayoutInflater.from(this).inflate(R.layout.endgame2, null)
            //val titleTextView = dialogView.findViewById<TextView>(R.id.text_title)
            //val messageTextView = dialogView.findViewById<TextView>(R.id.text_message)
            val replay = dialogView.findViewById<Button>(R.id.replay)
            val quit = dialogView.findViewById<Button>(R.id.quitgame2)
            stopScoreTimer()

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()

            replay.setOnClickListener {
                dialog.dismiss()

                life = 5
                val scoreTextView = findViewById<TextView>(R.id.lifeADV)
                scoreTextView.text = "Vie ADV: $life"

                life = 5
                //score()
                val heart2 = findViewById<ImageView>(R.id.heart2)
                val heart3 = findViewById<ImageView>(R.id.heart3)
                val heart4 = findViewById<ImageView>(R.id.heart4)
                val heart5 = findViewById<ImageView>(R.id.heart5)
                heart.visibility = View.VISIBLE
                heart2.visibility = View.VISIBLE
                heart3.visibility = View.VISIBLE
                heart4.visibility = View.VISIBLE
                heart5.visibility = View.VISIBLE

                duration = 3000

                arrayDuration = arrayOf(2000L,2500L, 3000L,3500, 4000)

            }
            quit.setOnClickListener {
                val intent = Intent(applicationContext, Game2::class.java)
                startActivity(intent)
                finish()
            }

            dialog.show()*/

        }
    }

    // Fonction pour faire vibrer le téléphone
    private fun vibratePhone(context: Context, milliseconds: Long) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Vérifie si le périphérique prend en charge la vibration
        if (vibrator.hasVibrator()) {
            // Vibre pendant le nombre de millisecondes spécifié
            vibrator.vibrate(milliseconds)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun startGame(){
        Handler(Looper.getMainLooper()).post {
            // Votre code d'animation ici

            moveFire()

            riseDifficulty()
            shuffleDelay()
            //score()


        }
    }
    /*private fun showGameOverDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.overonlinegame1, null)
        //val titleTextView = dialogView.findViewById<TextView>(R.id.text_title)
        val messageTextView = dialogView.findViewById<TextView>(R.id.result)
        val quitButton = dialogView.findViewById<Button>(R.id.quitter)
        val resumeButton = dialogView.findViewById<Button>(R.id.replay)

        //titleTextView.text = "Partie terminée"
        //messageTextView.text = "Que voulez-vous faire ?"

        println(isServer)
        println(isClient)
        println(serveurScore)
        println(clientScore)

        if (isServer && serveurScore>clientScore){
            messageTextView.text = "😏 Victoire 😎"
        }else if(isServer && serveurScore<clientScore){
            messageTextView.text = "😪 Défaite 😭"
        }
        if (isClient && serveurScore<clientScore){
            messageTextView.text = "😏 Victoire 😎"
        }else if(isClient && serveurScore>clientScore){
            messageTextView.text = "😪 Défaite 😭"
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        quitButton.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        resumeButton.setOnClickListener {
            dialog.dismiss()
            //startCountDownTimer()

            val scoreTextView = findViewById<TextView>(R.id.scoregame2)
            val scoreAdvTextView = findViewById<TextView>(R.id.scoregame2ADV)
            score = 0
            clientScore = 0
            serveurScore = 0
            scoreTextView.text = "MOI : $score"
            scoreAdvTextView.text = "ADV : $score"
        }

        dialog.show()
    }*/

}


