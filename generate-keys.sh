#!/bin/bash

KEYS_DIR=".keys"

PRIVATE_KEY="private.pem"
PUBLIC_KEY="public.pem"

if [ ! -d "$KEYS_DIR" ]; then
  echo "Creating directory $KEYS_DIR..."
  mkdir -p "$KEYS_DIR"
fi

echo "Generating RSA private key..."
openssl genrsa -out "$KEYS_DIR/$PRIVATE_KEY" 2048

echo "Extracting RSA public key..."
openssl rsa -in "$KEYS_DIR/$PRIVATE_KEY" -pubout -out "$KEYS_DIR/$PUBLIC_KEY"

echo "Setting permissions..."
chmod 600 "$KEYS_DIR/$PRIVATE_KEY"
chmod 644 "$KEYS_DIR/$PUBLIC_KEY"

echo "RSA keys successfully generated at:"
echo "  Private key: $KEYS_DIR/$PRIVATE_KEY"
echo "  Public key: $KEYS_DIR/$PUBLIC_KEY"
echo ""
echo "WARNING: These keys are sensitive. Do not share them or add them to version control."