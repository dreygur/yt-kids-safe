import 'package:flutter/material.dart';
import '../../config/theme.dart';
import '../profile_select_screen.dart';

class TimesUpScreen extends StatelessWidget {
  const TimesUpScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        width: double.infinity,
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [
              Color(0xFF2C3E50),
              Color(0xFF1A252F),
            ],
          ),
        ),
        child: SafeArea(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              // Moon
              const Text(
                '🌙',
                style: TextStyle(fontSize: 80),
              ),
              const SizedBox(height: 24),

              // Stars
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: const [
                  Text('⭐', style: TextStyle(fontSize: 24)),
                  SizedBox(width: 8),
                  Text('⭐', style: TextStyle(fontSize: 32)),
                  SizedBox(width: 8),
                  Text('⭐', style: TextStyle(fontSize: 24)),
                  SizedBox(width: 8),
                  Text('⭐', style: TextStyle(fontSize: 28)),
                  SizedBox(width: 8),
                  Text('⭐', style: TextStyle(fontSize: 20)),
                ],
              ),
              const SizedBox(height: 32),

              // Message
              const Text(
                'Time to take a break!',
                style: TextStyle(
                  fontSize: 28,
                  fontWeight: FontWeight.bold,
                  color: AppColors.white,
                ),
              ),
              const SizedBox(height: 16),

              const Padding(
                padding: EdgeInsets.symmetric(horizontal: 48),
                child: Text(
                  'Great watching today! Your eyes need some rest. See you tomorrow! 💤',
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontSize: 16,
                    color: AppColors.white,
                    height: 1.5,
                  ),
                ),
              ),
              const SizedBox(height: 48),

              // Button
              ElevatedButton(
                onPressed: () {
                  Navigator.of(context).pushAndRemoveUntil(
                    MaterialPageRoute(
                      builder: (_) => const ProfileSelectScreen(),
                    ),
                    (route) => false,
                  );
                },
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.accent2,
                  foregroundColor: AppColors.textPrimary,
                  padding: const EdgeInsets.symmetric(
                    horizontal: 32,
                    vertical: 16,
                  ),
                ),
                child: const Text(
                  'See you tomorrow! 👋',
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
