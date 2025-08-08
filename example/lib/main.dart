import 'package:flutter/material.dart';
import 'package:prepaidcard_reader/prepaidcard_reader.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  bool isWaitScan = false;
  bool isLoading = false;
  bool isCardSupported=false;
  CardModel? cardData;

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'NFC Card Reader',
      theme: ThemeData(
        primarySwatch: Colors.blue,
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
      home: Scaffold(
        appBar: AppBar(
          title: const Text('PrepaidCard Reader'),
          centerTitle: true,
          elevation: 0,
        ),
        body: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              // Header Section
              Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  color: Colors.blue[50],
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Column(
                  children: [
                    Icon(Icons.nfc, size: 60, color: Colors.blue[700]),
                    const SizedBox(height: 10),
                    Text(
                      isWaitScan ? 'Tap your NFC Card' : 'Ready to Scan',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                        color: Colors.blue[800],
                      ),
                    ),
                    const SizedBox(height: 5),
                    Text(
                      isWaitScan
                          ? 'Hold your card near the back of the device'
                          : 'Press the "Read Card" button to start',
                      textAlign: TextAlign.center,
                      style: TextStyle(color: Colors.grey[600], fontSize: 14),
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 30),

              // Action Button
              SizedBox(
                width: double.infinity,
                height: 50,
                child: ElevatedButton.icon(
                  onPressed: _handleScanButton,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: isWaitScan ? Colors.red : Colors.blue,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                    elevation: 2,
                  ),
                  icon: Icon(
                    isWaitScan ? Icons.stop : Icons.search_rounded,
                    color: Colors.white,
                  ),
                  label: Text(
                    isWaitScan ? "STOP SCANNING" : "READ CARD",
                    style: const TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                      color: Colors.white,
                    ),
                  ),
                ),
              ),

              const SizedBox(height: 30),

              // Loading Indicator
              if (isLoading)
                const Column(
                  children: [
                    CircularProgressIndicator(),
                    SizedBox(height: 10),
                    Text('Reading card...'),
                  ],
                ),

              // Card Data Display
              if (cardData != null && !isLoading) _buildCardInfo(),
              if(cardData == null && !isLoading && !isCardSupported) Text('Card not supported'),

              // Empty State
              if (cardData == null && !isLoading && !isWaitScan)
                Container(
                  padding: const EdgeInsets.all(20),
                  decoration: BoxDecoration(
                    border: Border.all(color: Colors.grey[300]!),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Column(
                    children: [
                      Icon(
                        Icons.credit_card_outlined,
                        size: 40,
                        color: Colors.grey[400],
                      ),
                      const SizedBox(height: 10),
                      Text(
                        'No card data yet',
                        style: TextStyle(color: Colors.grey[600], fontSize: 16),
                      ),
                      const SizedBox(height: 5),
                      Text(
                        'Card data will appear here after a successful scan',
                        textAlign: TextAlign.center,
                        style: TextStyle(color: Colors.grey[500], fontSize: 12),
                      ),
                    ],
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }

  void _handleScanButton() {
    if (isWaitScan) {
      // Stop scanning
      PrepaidcardReader.instance.stopSession();
      setState(() {
        isWaitScan = false;
        isLoading = false;
      });
    } else {
      // Start scanning
      setState(() {
        isWaitScan = true;
        isLoading = true;
        cardData = null;
      });

      PrepaidcardReader.instance.startSession(
        (card) async {
          setState(() {
            isLoading = false;
            isWaitScan = false;
            if (card.cardNumber.isEmpty) {
              cardData = null;
              isCardSupported = false;
            } else {
              cardData = card;
              isCardSupported = true;
            }
          });
        },
        stopOnDiscovered: true,
      ).catchError((error) {
            PrepaidcardReader.instance.stopSession();
            setState(() {
              isWaitScan = false;
              isLoading = false;
            });
            // Handle error if needed
          });
    }
  }

  Widget _buildCardInfo() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.2),
            spreadRadius: 1,
            blurRadius: 5,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(Icons.credit_card, color: Colors.blue[700], size: 28),
              const SizedBox(width: 10),
              Text(
                'Card Information',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                  color: Colors.blue[800],
                ),
              ),
            ],
          ),
          const Divider(height: 30),

          _buildInfoRow('Card Name', cardData?.cardName ?? '-'),
          const SizedBox(height: 15),
          _buildInfoRow('Card Number', cardData?.cardNumber ?? '-'),
          const SizedBox(height: 15),
          _buildInfoRow('Card Code', cardData?.cardCode ?? '-'),
          const SizedBox(height: 15),
          _buildInfoRow(
            'Balance',
            cardData?.balance != null ? 'Rp ${cardData?.balance}' : '-',
            isBold: true,
          ),

          const SizedBox(height: 20),
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: Colors.green[50],
              borderRadius: BorderRadius.circular(8),
              border: Border.all(color: Colors.green[200]!),
            ),
            child: Row(
              children: [
                Icon(Icons.check_circle, color: Colors.green[700], size: 20),
                const SizedBox(width: 8),
                Text(
                  'Read successfully',
                  style: TextStyle(
                    color: Colors.green[800],
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildInfoRow(String label, String value, {bool isBold = false}) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SizedBox(
          width: 120,
          child: Text(
            label,
            style: TextStyle(color: Colors.grey[600], fontSize: 14),
          ),
        ),
        const Text(': '),
        Expanded(
          child: Text(
            value,
            style: TextStyle(
              fontWeight: isBold ? FontWeight.bold : FontWeight.normal,
              fontSize: 14,
              color: isBold ? Colors.green[700] : Colors.black87,
            ),
          ),
        ),
      ],
    );
  }
}
