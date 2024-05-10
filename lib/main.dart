import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Flutter Demo',
      theme: ThemeData(
        // This is the theme of your application.
        //
        // TRY THIS: Try running your application with "flutter run". You'll see
        // the application has a blue toolbar. Then, without quitting the app,
        // try changing the seedColor in the colorScheme below to Colors.green
        // and then invoke "hot reload" (save your changes or press the "hot
        // reload" button in a Flutter-supported IDE, or press "r" if you used
        // the command line to start the app).
        //
        // Notice that the counter didn't reset back to zero; the application
        // state is not lost during the reload. To reset the state, use hot
        // restart instead.
        //
        // This works for code too, not just values: Most code changes can be
        // tested with just a hot reload.
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: ProximityWidget(),
    );
  }
}

class ProximityWidget extends StatefulWidget {
  @override
  _ProximityWidgetState createState() => _ProximityWidgetState();
}

class _ProximityWidgetState extends State<ProximityWidget> {
  static const proxPlateform =
      MethodChannel("samples.flutter.dev/proximitysensor");
  static const platform =   MethodChannel('example_service');
  String _serverState = 'Did not make the call yet';

  late StreamSubscription _proximitySubscription;
  bool isNear = false;
  Future<void> _startService() async {
    try {
      final result = await platform.invokeMethod('startExampleService');
      setState(() {
        _serverState = result;
      });
    } on PlatformException catch (e) {
      print("Failed to invoke method: '${e.message}'.");
    }
  }

  Future<void> _stopService() async {
    try {
      final result = await platform.invokeMethod('stopExampleService');
      setState(() {
        _serverState = result;
      });
    } on PlatformException catch (e) {
      print("Failed to invoke method: '${e.message}'.");
    }
  }

  @override
  void initState() {
    super.initState();
    // Subscribe to the proximity stream when the widget initializes
    _proximitySubscription = proximityStream.listen((proximityData) {
      setState(() {
        // Update the state based on the received data
        isNear = proximityData;
      });
    });
  }

  @override
  void dispose() {
    _proximitySubscription.cancel(); // Cancel the stream subscription
    super.dispose();
  }

  Stream<bool> get proximityStream async* {
    try {
      while (true) {
        final bool result =
            await proxPlateform.invokeMethod('getisFlashlightState');
        yield result; // Yield the received proximity data
        // await Future.delayed(Duration(seconds: 1)); // Delay between each check
       }
    } on PlatformException catch (e) {
      print("Failed to get proximity data: '${e.message}'.");
      // Handle error
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: isNear ? Color.fromRGBO (58, 58, 58, 1) : Colors.white,
      appBar: AppBar(
        backgroundColor: isNear ? Color.fromRGBO(97, 97, 97, 1) : Colors.white,
       ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.start,
          children: [
            SizedBox(height: 50),

            ElevatedButton(
              child: Text('Start Service'),
              onPressed: _startService,
            ),
            ElevatedButton(
              child: Text('Stop Service'),
              onPressed: _stopService,
            ),
            SizedBox(height: 50),

      
            Container(
              height: 250,
              width: 250,
              child: Image.asset(isNear
                  ? 'assets/flashlight_on.png'
                  : 'assets/flashlight_off.png'),
            ),

            // Text(_serverState),
          ],
        ),
      ),
    );
  }
}
