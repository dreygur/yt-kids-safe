// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'profile.dart';

// **************************************************************************
// TypeAdapterGenerator
// **************************************************************************

class ProfileAdapter extends TypeAdapter<Profile> {
  @override
  final int typeId = 0;

  @override
  Profile read(BinaryReader reader) {
    final numOfFields = reader.readByte();
    final fields = <int, dynamic>{
      for (int i = 0; i < numOfFields; i++) reader.readByte(): reader.read(),
    };
    return Profile(
      id: fields[0] as String,
      name: fields[1] as String,
      avatar: fields[2] as String,
      dailyLimitMinutes: fields[3] as int,
      usedTodayMinutes: fields[4] as int,
      lastResetDate: fields[5] as DateTime,
      allowedChannelIds: (fields[6] as List).cast<String>(),
      allowedPlaylistIds: (fields[7] as List).cast<String>(),
    );
  }

  @override
  void write(BinaryWriter writer, Profile obj) {
    writer
      ..writeByte(8)
      ..writeByte(0)
      ..write(obj.id)
      ..writeByte(1)
      ..write(obj.name)
      ..writeByte(2)
      ..write(obj.avatar)
      ..writeByte(3)
      ..write(obj.dailyLimitMinutes)
      ..writeByte(4)
      ..write(obj.usedTodayMinutes)
      ..writeByte(5)
      ..write(obj.lastResetDate)
      ..writeByte(6)
      ..write(obj.allowedChannelIds)
      ..writeByte(7)
      ..write(obj.allowedPlaylistIds);
  }

  @override
  int get hashCode => typeId.hashCode;

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is ProfileAdapter &&
          runtimeType == other.runtimeType &&
          typeId == other.typeId;
}
