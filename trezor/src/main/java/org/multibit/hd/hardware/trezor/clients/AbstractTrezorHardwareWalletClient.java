package org.multibit.hd.hardware.trezor.clients;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.Transaction;
import com.google.common.base.Optional;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.satoshilabs.trezor.protobuf.TrezorMessage;
import com.satoshilabs.trezor.protobuf.TrezorType;
import org.multibit.hd.hardware.core.HardwareWalletClient;
import org.multibit.hd.hardware.core.events.HardwareWalletEvent;

import java.util.concurrent.TimeUnit;

/**
 * <p>Hardware wallet client to provide the following to Trezor clients:</p>
 * <ul>
 * <li>Access to common methods</li>
 * </ul>
 *
 * @since 0.0.1
 *  
 */
public abstract class AbstractTrezorHardwareWalletClient implements HardwareWalletClient {

  @Override
  public Optional<HardwareWalletEvent> initialize() {
    return sendMessage(
      TrezorMessage.Initialize
        .newBuilder()
        .build()
    );
  }

  @Override
  public Optional<HardwareWalletEvent> ping() {
    return sendMessage(
      TrezorMessage.Ping
        .newBuilder()
        .build()
    );
  }

  @Override
  public Optional<HardwareWalletEvent> clearSession() {
    return sendMessage(
      TrezorMessage.ClearSession
        .newBuilder()
        .build()
    );
  }

  @Override
  public Optional<HardwareWalletEvent> changePin(boolean remove) {

    return sendMessage(
      TrezorMessage.ChangePin
        .newBuilder()
        .setRemove(remove)
        .build()
    );
  }

  @Override
  public Optional<HardwareWalletEvent> wipeDevice() {
    return sendMessage(
      TrezorMessage.WipeDevice
        .newBuilder()
        .build()
    );
  }

  @Override
  public Optional<HardwareWalletEvent> firmwareErase() {
    throw new UnsupportedOperationException("Use the mytrezor.com website for firmware upgrades.");
  }

  @Override
  public Optional<HardwareWalletEvent> firmwareUpload() {
    throw new UnsupportedOperationException("Use the mytrezor.com website for firmware upgrades.");
  }

  @Override
  public Optional<HardwareWalletEvent> getEntropy() {
    return sendMessage(
      TrezorMessage.GetEntropy
        .newBuilder()
        .build()
    );
  }

  @Override
  public Optional<HardwareWalletEvent> getPublicKey(int index, int value, Optional<String> coinName) {

    // The master public key normally takes up to 10 seconds to complete
    return sendMessage(
      TrezorMessage.GetPublicKey
        .newBuilder()
        .setAddressN(index, value)
        .build()
    );
  }

  @Override
  public Optional<HardwareWalletEvent> loadDevice(
    String language,
    String seed,
    String pin,
    boolean passphraseProtection
  ) {

    // Define the node
    TrezorType.HDNodeType nodeType = TrezorType.HDNodeType
      .newBuilder()
      .setChainCode(ByteString.copyFromUtf8(""))
      .setChildNum(0)
      .setDepth(0)
      .setFingerprint(0)
      .build();

    // A load normally takes about 10 seconds to complete
    return sendMessage(TrezorMessage.LoadDevice
        .newBuilder()
        .setMnemonic(seed)
        .setLanguage(language)
        .setNode(nodeType)
        .setPin(pin)
        .setPassphraseProtection(passphraseProtection)
        .build()
    );

  }

  @Override
  public Optional<HardwareWalletEvent> resetDevice(
    String language,
    String label,
    boolean displayRandom,
    boolean passphraseProtection,
    boolean pinProtection,
    int strength
  ) {

    return sendMessage(
      TrezorMessage.ResetDevice
        .newBuilder()
        .setLanguage(language)
        .setLabel(label)
        .setDisplayRandom(displayRandom)
        .setPassphraseProtection(passphraseProtection)
        .setStrength(strength)
        .setPinProtection(pinProtection)
        .build()
    );

  }

  @Override
  public Optional<HardwareWalletEvent> recoverDevice(String language, String label, int wordCount, boolean passphraseProtection, boolean pinProtection) {
    return sendMessage(
      TrezorMessage.RecoveryDevice
        .newBuilder()
        .setLanguage(language)
        .setLabel(label)
        .setWordCount(wordCount)
        .setPassphraseProtection(passphraseProtection)
        .setPinProtection(pinProtection)
        .build()
    );
  }

  @Override
  public Optional<HardwareWalletEvent> wordAck(String word) {
    return sendMessage(
      TrezorMessage.WordAck
        .newBuilder()
        .setWord(word)
        .build()
    );
  }

  @Override
  public Optional<Transaction> signTx(Transaction tx) {

    // TODO (GR) This is currently unmodified
    return Optional.of(tx);

  }

  @Override
  public Optional<Transaction> simpleSignTx(Transaction tx) {
    // TODO (GR) This is currently unmodified
    return Optional.of(tx);
  }

  @Override
  public Optional<HardwareWalletEvent> pinMatrixAck(String pin) {
    return sendMessage(
      TrezorMessage.PinMatrixAck
        .newBuilder()
        .setPin(pin)
        .build()
    );
  }

  @Override
  public Optional<HardwareWalletEvent> buttonAck() {
    return sendMessage(
      TrezorMessage.ButtonAck
        .newBuilder()
        .build(),
      // Allow user time to decide
      10, TimeUnit.MINUTES
    );
  }

  public Optional<HardwareWalletEvent> cancel() {
    return sendMessage(
      TrezorMessage.Cancel
        .newBuilder()
        .build()
    );
  }

  @Override
  public Optional<HardwareWalletEvent> getAddress(int index, int value, boolean showDisplay) {
    return sendMessage(
      TrezorMessage.GetAddress
        .newBuilder()
        .setAddressN(index, value)
        .setCoinName("Bitcoin")
        .setShowDisplay(showDisplay)
        .build()
    );
  }

  @Override
  public Optional<HardwareWalletEvent> applySettings(String language, String label) {

    return sendMessage(
      TrezorMessage.ApplySettings
        .newBuilder()
        .setLanguage(language)
        .setLabel(label)
        .build()
    );
  }

  @Override
  public Optional<HardwareWalletEvent> entropyAck(byte[] entropy) {
    return sendMessage(
      TrezorMessage.EntropyAck
        .newBuilder()
        .setEntropy(ByteString.copyFrom(entropy))
        .build()
    );
  }

  @Override
  public Optional<HardwareWalletEvent> signMessage(int index, int value, byte[] message) {
    return sendMessage(
      TrezorMessage.SignMessage
        .newBuilder()
        .setAddressN(index, value)
        .setCoinName("Bitcoin")
        .setMessage(ByteString.copyFrom(message))
        .build()
    );
  }

  @Override
  public Optional<HardwareWalletEvent> verifyMessage(Address address, byte[] signature, byte[] message) {
    return sendMessage(
      TrezorMessage.VerifyMessage
        .newBuilder()
        .setAddress(address.toString())
        .setSignature(ByteString.copyFrom(signature))
        .setMessage(ByteString.copyFrom(message))
        .build()
    );
  }

  @Override
  public Optional<HardwareWalletEvent> encryptMessage(byte[] pubKey, byte[] message, boolean displayOnly) {
    return sendMessage(
      TrezorMessage.EncryptMessage
        .newBuilder()
        .setPubkey(ByteString.copyFrom(pubKey))
        .setMessage(ByteString.copyFrom(message))
        .setDisplayOnly(displayOnly)
        .build()
    );
  }

  @Override
  public Optional<HardwareWalletEvent> decryptMessage(int index, int value, byte[] message) {
    return sendMessage(
      TrezorMessage.DecryptMessage
        .newBuilder()
        .setAddressN(index, value)
        .setMessage(ByteString.copyFrom(message))
        .build()
    );
  }

  @Override
  public Optional<HardwareWalletEvent> cipherKeyValue(int index, int value, byte[] key, byte[] keyValue, boolean encrypt, boolean askOnDecrypt, boolean askOnEncrypt) {
    return sendMessage(
      TrezorMessage.CipherKeyValue
        .newBuilder()
        .setAddressN(index, value)
        .setAskOnDecrypt(askOnDecrypt)
        .setAskOnEncrypt(askOnEncrypt)
        .setEncrypt(encrypt)
        .setKeyBytes(ByteString.copyFrom(key))
        .setValue(ByteString.copyFrom(keyValue))
        .build()
    );
  }

  @Override
  public Optional<HardwareWalletEvent> passphraseAck(String passphrase) {
    return sendMessage(
      TrezorMessage.PassphraseAck
        .newBuilder()
        .setPassphrase(passphrase)
        .build()
    );
  }

  @Override
  public Optional<HardwareWalletEvent> estimateTxSize(Transaction tx) {

    int inputsCount = tx.getInputs().size();
    int outputsCount = tx.getOutputs().size();

    return sendMessage(
      TrezorMessage.EstimateTxSize
        .newBuilder()
        .setCoinName("Bitcoin")
        .setInputsCount(inputsCount)
        .setOutputsCount(outputsCount)
        .build()
    );
  }

  /**
   * <p>Send a message to the device that should have a near-immediate (under 1 second) response.</p>
   * <p>If the response times out a FAILURE message should be generated.</p>
   *
   * @param messsage The message to send to the hardware wallet
   *
   * @return An optional hardware wallet event, present only in blocking implementations
   */
  protected abstract Optional<HardwareWalletEvent> sendMessage(Message messsage);

  /**
   * <p>Send a message to the device with an arbitrary response duration.</p>
   * <p>If the response times out a FAILURE message should be generated.</p>
   *
   * @param messsage The message to send to the hardware wallet
   *
   * @return An optional hardware wallet event, present only in blocking implementations
   */
  protected abstract Optional<HardwareWalletEvent> sendMessage(Message messsage, int duration, TimeUnit timeUnit);

}