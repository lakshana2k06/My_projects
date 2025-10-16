import joblib
import numpy as np
from tensorflow.keras.models import load_model

def load_models(symbol):
    lstm_model = load_model(f'models/{symbol}_lstm.keras')
    xgb_model = joblib.load(f'models/{symbol}_xgboost.pkl')
    scaler = joblib.load(f'models/{symbol}_scaler.pkl')
    return lstm_model, xgb_model, scaler

def predict(symbol, features):
    lstm_model, xgb_model, scaler = load_models(symbol)

    # Scale input
    scaled_input = scaler.transform([features])
    
    # Prepare LSTM input (reshape to 3D)
    lstm_input = np.reshape(scaled_input, (1, scaled_input.shape[1], 1))
    lstm_pred = lstm_model.predict(lstm_input)[0][0]

    xgb_pred = xgb_model.predict(scaled_input)[0]

    return {
        "lstm_prediction": float(lstm_pred),
        "xgboost_prediction": float(xgb_pred)
    }
