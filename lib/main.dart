import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'config/theme.dart';
import 'services/storage_service.dart';
import 'services/time_tracker.dart';
import 'screens/profile_select_screen.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Lock orientation to portrait
  await SystemChrome.setPreferredOrientations([
    DeviceOrientation.portraitUp,
    DeviceOrientation.portraitDown,
  ]);

  // Initialize storage
  await StorageService.init();

  runApp(const YTKidsSafeApp());
}

class YTKidsSafeApp extends StatelessWidget {
  const YTKidsSafeApp({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => TimeTracker(),
      child: MaterialApp(
        title: 'YT Kids Safe',
        debugShowCheckedModeBanner: false,
        theme: AppTheme.lightTheme,
        home: const ProfileSelectScreen(),
      ),
    );
  }
}
