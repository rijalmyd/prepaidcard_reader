import 'dart:async';
import 'dart:convert';
import 'package:flutter/services.dart';

typedef NfcTagCallback = Future<void> Function(CardModel cardModel);
typedef NfcErrorCallback = Future<void> Function(NfcError error);

class PrepaidcardReader {
  static const MethodChannel channel = MethodChannel('read_card_nfc');
  PrepaidcardReader._() {
    channel.setMethodCallHandler(_handleMethodCall);
  }
  static PrepaidcardReader? _instance;

  static PrepaidcardReader get instance => _instance ??= PrepaidcardReader._();

  NfcTagCallback? _onDiscovered;

  NfcErrorCallback? onError;

  Future<bool> isAvailable() async {
    return channel
        .invokeMethod<bool>('Nfc#isAvailable')
        .then<bool>((bool? value) => value ?? false);
  }

  Future<void> startSession(
    NfcTagCallback onDiscovered, {
    Set<NfcPollingOption>? pollingOptions,
    String alertMessage = "",
    NfcErrorCallback? onError,
  }) async {
    _onDiscovered = onDiscovered;
    onError = onError;
    pollingOptions ??= NfcPollingOption.values.toSet();
    return channel.invokeMethod('Nfc#startSession', {
      'alertMessage': alertMessage,
    });
  }

  Future<void> stopSession({
    String alertMessage = "",
    String errorMessage = "",
  }) async {
    _onDiscovered = null;
    onError = null;
    return channel.invokeMethod('Nfc#stopSession', {
      'alertMessage': alertMessage,
      'errorMessage': errorMessage,
    });
  }

  Future<void> disposeTag(String handle) async {
    return channel.invokeMethod('Nfc#disposeTag', {'handle': handle});
  }

  Future<void> _handleMethodCall(MethodCall call) async {
    switch (call.method) {
      case 'onDiscovered':
        _handleOnDiscovered(call);
        break;
      case 'onError':
        _handleOnError(call);
        break;
      default:
        throw ('Not implemented: ${call.method}');
    }
  }

  void _handleOnDiscovered(MethodCall call) async {
    try {
      CardModel cardModel = CardModel.fromJson(jsonDecode(call.arguments));
      if (_onDiscovered != null) {
        if (_onDiscovered != null) await _onDiscovered!(cardModel);
      }
    } catch (e) {
      if (_onDiscovered != null) {
        _onDiscovered!(
          CardModel(
            "",
            "",
            "",
            "",
            "$e",
            '',
          ),
        );
      }
    }
  }

  void _handleOnError(MethodCall call) async {
  }
}

class NfcTag {
  const NfcTag(this.handle, this.data);

  final String handle;

  final Map<String, dynamic> data;
}

class NfcError {
  const NfcError(this.type, this.message, this.details);

  final NfcErrorType type;

  final String message;

  final dynamic details;
}

enum NfcPollingOption { iso14443, iso15693, iso18092 }

enum NfcErrorType { sessionTimeout, systemIsBusy, userCanceled, unknown }

class CardModel {
  String cardId;
  String cardName;
  String cardCode;
  String cardNumber;
  String balance;
  String anotherInfo;
  CardModel(
    this.cardId,
    this.cardName,
    this.cardNumber,
    this.balance,
    this.anotherInfo,
    this.cardCode,
  );
  factory CardModel.fromJson(Map<String, dynamic> json) {
    return CardModel(
      json['card_id'],
      json['card_name'],
      json['card_number'],
      json['balance'],
      json['another_info'],
      json['card_code'],
    );
  }
  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = <String, dynamic>{};
    data['card_id'] = cardId;
    data['card_name'] = cardName;
    data['card_number'] = cardNumber;
    data['balance'] = balance;
    data['another_info'] = anotherInfo;
    return data;
  }
}
