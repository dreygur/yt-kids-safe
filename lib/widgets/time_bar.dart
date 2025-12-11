import 'package:flutter/material.dart';
import '../config/theme.dart';
import '../models/profile.dart';

class TimeBar extends StatelessWidget {
  final Profile profile;

  const TimeBar({super.key, required this.profile});

  @override
  Widget build(BuildContext context) {
    final remaining = profile.remainingMinutes;
    final total = profile.dailyLimitMinutes;
    final progress = remaining / total;
    final isLow = remaining <= 15;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Icon(
              Icons.timer_outlined,
              size: 14,
              color: isLow ? AppColors.accent2 : AppColors.white,
            ),
            const SizedBox(width: 4),
            Text(
              '$remaining min left',
              style: TextStyle(
                fontSize: 12,
                color: isLow ? AppColors.accent2 : AppColors.white,
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
        ),
        const SizedBox(height: 4),
        Container(
          height: 6,
          width: 120,
          decoration: BoxDecoration(
            color: AppColors.white.withOpacity(0.3),
            borderRadius: BorderRadius.circular(3),
          ),
          child: FractionallySizedBox(
            alignment: Alignment.centerLeft,
            widthFactor: progress.clamp(0.0, 1.0),
            child: Container(
              decoration: BoxDecoration(
                color: isLow ? AppColors.accent2 : AppColors.white,
                borderRadius: BorderRadius.circular(3),
              ),
            ),
          ),
        ),
      ],
    );
  }
}
