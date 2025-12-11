import 'dart:async';
import 'package:flutter/foundation.dart';
import '../models/profile.dart';
import 'storage_service.dart';

class TimeTracker extends ChangeNotifier {
  Timer? _timer;
  DateTime? _startTime;
  Profile? _currentProfile;
  bool _isTracking = false;

  bool get isTracking => _isTracking;
  Profile? get currentProfile => _currentProfile;

  int get currentSessionMinutes {
    if (_startTime == null) return 0;
    return DateTime.now().difference(_startTime!).inMinutes;
  }

  void setProfile(Profile? profile) {
    if (_isTracking) {
      stopTracking();
    }
    _currentProfile = profile;
    notifyListeners();
  }

  void startTracking() {
    if (_currentProfile == null || _isTracking) return;

    _isTracking = true;
    _startTime = DateTime.now();

    // Update every minute
    _timer = Timer.periodic(const Duration(minutes: 1), (_) {
      _currentProfile?.addWatchTime(1);
      notifyListeners();

      // Check if time is up
      if (_currentProfile != null && !_currentProfile!.hasTimeRemaining) {
        stopTracking();
      }
    });

    notifyListeners();
  }

  void stopTracking() {
    if (!_isTracking) return;

    _timer?.cancel();
    _timer = null;

    // Add remaining seconds as partial minute
    if (_startTime != null && _currentProfile != null) {
      final elapsed = DateTime.now().difference(_startTime!);
      final remainingSeconds = elapsed.inSeconds % 60;
      if (remainingSeconds >= 30) {
        _currentProfile!.addWatchTime(1);
      }
    }

    _isTracking = false;
    _startTime = null;
    notifyListeners();
  }

  void pauseTracking() {
    _timer?.cancel();
    _timer = null;
    _isTracking = false;
    notifyListeners();
  }

  void resumeTracking() {
    if (_currentProfile == null || !_currentProfile!.hasTimeRemaining) return;
    startTracking();
  }

  @override
  void dispose() {
    stopTracking();
    super.dispose();
  }
}
