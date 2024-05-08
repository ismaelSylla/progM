package com.example.myapplication.game1

import android.Manifest
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
import com.example.myapplication.game2.Game2
import java.io.IOException
import java.util.UUID


class MultiPlayers : AppCompatActivity(), AdapterView.OnItemClickListener {
    private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FA")
    private  var Ownsocket: BluetoothSocket? = null
    private val REQUEST_ENABLE_BT = 1
    private lateinit var bluetoothAdapter: BluetoothAdapter
    lateinit var bluetoothManager: BluetoothManager
    private lateinit var deviceListAdapter: ArrayAdapter<String>
    private var deviceList = ArrayList<String>()
    private var deviceListAdr = ArrayList<String>()

    lateinit var timerTextView: TextView
    private lateinit var countDownTimer: CountDownTimer
    private var horizontalBias: Float = 0.547f
    private var score: Int = 0
    private var currentImagePosition: Float = 0f
    private lateinit var oursocket: BluetoothSocket

    private var isServer = false
    private var serveurScore: Int = 0
    private var isClient = false
    private var clientScore: Int = 0

    lateinit var mediaPlayer_suc: MediaPlayer
    lateinit var mediaPlayer_fail: MediaPlayer

    companion object {
        const val NAME = "YourBluetoothServerName"
        private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //setContentView(R.layout.popup_invite_bluetooth)
        setContentView(R.layout.multiplayer)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.multiplayer)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        mediaPlayer_suc = MediaPlayer.create(this, R.raw.succes)
        mediaPlayer_fail = MediaPlayer.create(this, R.raw.fail)

        val imageView: ImageView = findViewById(R.id.maflechette)

        imageView.setOnTouchListener { view, event ->
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
        }

        // Register for broadcasts when a device is discovered.
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)

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
        countDownTimer.cancel()

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
                            val scoreAdvTextView = findViewById<TextView>(R.id.scoreADV)
                            clientScore= receivedMessage.substringAfter("ADV : ").toInt()
                            scoreAdvTextView.text = "ADV : $clientScore"
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
                                val scoreAdvTextView = findViewById<TextView>(R.id.scoreADV)
                                serveurScore= message.substringAfter("ADV : ").toInt()
                                scoreAdvTextView.text = "ADV : ${serveurScore}"
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

    @SuppressLint("ClickableViewAccessibility")
    private fun startGame(){
        Handler(Looper.getMainLooper()).post {
            // Votre code d'animation ici


            startCountDownTimer()
            moveImage()



            val quit = findViewById<Button>(R.id.quit)
            val launch = findViewById<Button>(R.id.launch)

            quit.setOnClickListener{
                Toast.makeText(this, "Demarrage du jeu de l'arc", Toast.LENGTH_SHORT).show()
                val intent = Intent(applicationContext, Game1::class.java)
                startActivity(intent)
                finish()
            }

            launch.setOnClickListener{
                //Toast.makeText(this, "Lancer de l'Arc", Toast.LENGTH_SHORT).show()
                launch()
            }
        }
    }

    private fun moveImage() {
        val imageView = findViewById<ImageView>(R.id.imageView1)
        val screenWidth = resources.displayMetrics.widthPixels

        val dpValue = 145 // La valeur en dp que vous souhaitez convertir
        val density = resources.displayMetrics.density
        val imageWidth = (dpValue * density + 0.5f).toInt()

        val initialX = -imageWidth.toFloat()
        val finalX = screenWidth.toFloat()

        val animator = ObjectAnimator.ofFloat(imageView, "translationX", initialX, finalX)
        animator.duration = 5000 // Durée de l'animation en millisecondes
        animator.repeatCount = ValueAnimator.INFINITE
        animator.interpolator = LinearInterpolator()

        animator.addUpdateListener { animation ->
            val currentValue = animation.animatedValue as Float
            currentImagePosition = animation.animatedValue as Float

            if (currentValue >= finalX) {
                // Si l'image est arrivée à la fin de l'écran, inverse la direction
                animator.setFloatValues(finalX, initialX)
                println("Update droit->gauche")
            } else if (currentValue <= initialX) {
                // Si l'image est arrivée au début de l'écran, inverse la direction
                animator.setFloatValues(initialX, finalX)
                println("Update gauche->droit")
            }
        }

        animator.start()
    }

    private fun startCountDownTimer() {
        timerTextView = findViewById(R.id.time)

        countDownTimer = object : CountDownTimer(20000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Met à jour le texte du TextView avec le temps restant
                val secondsLeft = millisUntilFinished / 1000
                timerTextView.text = "$secondsLeft s"
            }

            override fun onFinish() {
                showGameOverDialog()
            }
        }
        // Démarre le compte à rebours
        countDownTimer.start()
    }

    /*private fun showGameOverDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Partie terminée")
            .setMessage("Que voulez-vous faire ?")
            .setPositiveButton("Quitter") { dialog, _ ->
                dialog.dismiss()
                finish() // Quitte l'application
            }
            .setNegativeButton("Reprendre") { dialog, _ ->
                dialog.dismiss()
                //startCountDownTimer() // Reprend la partie en redémarrant le compte à rebours
            }
            .setCancelable(false) // Empêche de fermer le dialogue en cliquant en dehors
            .show()
    }*/

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
        println(serveurScore)
        println(clientScore)

        if (isServer && serveurScore>clientScore){
            messageTextView.text = "😏 Victoire 😎"
            mediaPlayer_suc.start()
        }else if(isServer && serveurScore<clientScore){
            messageTextView.text = "😪 Défaite 😭"
            mediaPlayer_fail.start()
        }else if(isServer && serveurScore==clientScore){
            messageTextView.text = "😪 Match Nul 😭"
            mediaPlayer_fail.start()
        }
        if (isClient && serveurScore<clientScore){
            messageTextView.text = "😏 Victoire 😎"
            mediaPlayer_suc.start()
        }else if(isClient && serveurScore>clientScore){
            messageTextView.text = "😪 Défaite 😭"
            mediaPlayer_fail.start()
        }else if(isClient && serveurScore==clientScore){
            messageTextView.text = "😪 Match Nul 😭"
            mediaPlayer_fail.start()

        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        quitButton.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(applicationContext, Game1::class.java)
            startActivity(intent)
            finish()
        }

        resumeButton.setOnClickListener {
            dialog.dismiss()
            startCountDownTimer()

            val scoreTextView = findViewById<TextView>(R.id.score)
            val scoreAdvTextView = findViewById<TextView>(R.id.scoreADV)
            score = 0
            clientScore = 0
            serveurScore = 0
            scoreTextView.text = "MOI : $score"
            scoreAdvTextView.text = "ADV : $score"
        }

        dialog.show()
    }

    private fun launch(){
        val redDot = ImageView(this)
        redDot.setImageResource(R.drawable.red_dot)

        //println("horrizontal Bias:"+ horizontalBias)
        // Récupérer les LayoutParams du parent
        val parentLayout = findViewById<ConstraintLayout>(R.id.multiplayer)

        val params = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        params.verticalBias = 0.525f
        params.horizontalBias = horizontalBias


        redDot.layoutParams = params

        parentLayout.addView(redDot)
        scoring()

        val timer = object : CountDownTimer(1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Ne fait rien pendant le compte à rebours
            }

            override fun onFinish() {
                // Supprime le point rouge après 1,5 secondes
                parentLayout.removeView(redDot)
            }
        }
        timer.start()
    }

    private fun scoring(){
        val parentLayout = findViewById<ConstraintLayout>(R.id.multiplayer)
        val parentWidth = parentLayout.width.toFloat()
        val dpValue = 145 // La valeur en dp de ma cible
        val density = resources.displayMetrics.density
        val imageWidth = (dpValue * density + 0.5f).toInt()
        val posPoint = horizontalBias*parentWidth
        val posCible = currentImagePosition+imageWidth/2

        val scoreTextView = findViewById<TextView>(R.id.score)
        val scoreAdvTextView = findViewById<TextView>(R.id.scoreADV)


        if(posPoint < (posCible + 1*imageWidth/12) && posPoint > (posCible - 1*imageWidth/12)){
            score += 5
        }else if(posPoint < (posCible + 2*imageWidth/12) && posPoint > (posCible - 2*imageWidth/12)){
            score += 4
        }else if(posPoint < (posCible + 3*imageWidth/12) && posPoint > (posCible - 3*imageWidth/12)){
            score += 3
        }else if(posPoint < (posCible + 4*imageWidth/12) && posPoint > (posCible - 4*imageWidth/12)){
            score += 2
        }else if(posPoint < (posCible + 5*imageWidth/12) && posPoint > (posCible - 5*imageWidth/12)){
            score += 1
        }else if(posPoint < (posCible + 6*imageWidth/12) && posPoint > (posCible - 6*imageWidth/12)){
            score += 0
        }
        scoreTextView.text = "MOI : $score"
        //scoreAdvTextView.text = "Adv = $score"
        if (isClient) {
            clientScore = score
            println("le client envoie son score")
            ConnectThread(oursocket).sendMessage("ADV : $score")
        }
        if (isServer) {
            serveurScore = score
            println("le server envoie son score")
            AcceptThread(null).sendMessage("ADV : $score")
        }
    }

}


