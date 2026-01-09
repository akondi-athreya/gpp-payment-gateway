import { useState, useEffect } from 'react';
import apiService from '../services/api';
import './Dashboard.css';

function Dashboard() {
  const [merchant, setMerchant] = useState(null);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState('create-order');

  // Form states
  const [orderForm, setOrderForm] = useState({
    amount: '',
    receipt: '',
    notes: '',
  });

  const [paymentForm, setPaymentForm] = useState({
    order_id: '',
    method: 'upi',
    vpa: '',
    card: {
      number: '',
      expiry_month: '',
      expiry_year: '',
      cvv: '',
      holder_name: '',
    },
  });

  const [createdOrder, setCreatedOrder] = useState(null);
  const [createdPayment, setCreatedPayment] = useState(null);

  useEffect(() => {
    loadMerchantData();
  }, []);

  const loadMerchantData = async () => {
    try {
      const data = await apiService.getTestMerchant();
      setMerchant(data);
      setLoading(false);
    } catch (err) {
      setError('Failed to load merchant data: ' + err.message);
      setLoading(false);
    }
  };

  const handleCreateOrder = async (e) => {
    e.preventDefault();
    setError(null);
    setCreatedOrder(null);

    try {
      const orderData = {
        amount: parseInt(orderForm.amount),
        currency: 'INR',
        receipt: orderForm.receipt,
        notes: orderForm.notes ? JSON.parse(orderForm.notes) : {},
      };

      const order = await apiService.createOrder(orderData);
      setCreatedOrder(order);
      setPaymentForm({ ...paymentForm, order_id: order.id });
      setOrderForm({ amount: '', receipt: '', notes: '' });
    } catch (err) {
      setError('Failed to create order: ' + err.message);
    }
  };

  const handleCreatePayment = async (e) => {
    e.preventDefault();
    setError(null);
    setCreatedPayment(null);

    try {
      const paymentData = {
        order_id: paymentForm.order_id,
        method: paymentForm.method,
      };

      if (paymentForm.method === 'upi') {
        paymentData.vpa = paymentForm.vpa;
      } else if (paymentForm.method === 'card') {
        paymentData.card = {
          number: paymentForm.card.number,
          expiry_month: parseInt(paymentForm.card.expiry_month),
          expiry_year: parseInt(paymentForm.card.expiry_year),
          cvv: paymentForm.card.cvv,
          holder_name: paymentForm.card.holder_name,
        };
      }

      const payment = await apiService.createPayment(paymentData);
      setCreatedPayment(payment);
      
      // Poll for payment status
      pollPaymentStatus(payment.id);
    } catch (err) {
      setError('Failed to create payment: ' + err.message);
    }
  };

  const pollPaymentStatus = async (paymentId) => {
    const maxAttempts = 20;
    let attempts = 0;

    const interval = setInterval(async () => {
      attempts++;
      try {
        const payment = await apiService.getPayment(paymentId);
        setCreatedPayment(payment);

        if (payment.status !== 'processing' || attempts >= maxAttempts) {
          clearInterval(interval);
        }
      } catch (err) {
        console.error('Error polling payment status:', err);
        clearInterval(interval);
      }
    }, 2000);
  };

  if (loading) {
    return <div className="dashboard-container"><div className="loading">Loading...</div></div>;
  }

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <h1>Payment Gateway Dashboard</h1>
        {merchant && (
          <div className="merchant-info">
            <p><strong>Merchant:</strong> {merchant.name}</p>
            <p><strong>Email:</strong> {merchant.email}</p>
            <p><strong>API Key:</strong> {merchant.api_key}</p>
          </div>
        )}
      </header>

      <div className="dashboard-tabs">
        <button
          className={activeTab === 'create-order' ? 'tab-active' : ''}
          onClick={() => setActiveTab('create-order')}
        >
          Create Order
        </button>
        <button
          className={activeTab === 'create-payment' ? 'tab-active' : ''}
          onClick={() => setActiveTab('create-payment')}
        >
          Create Payment
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="dashboard-content">
        {activeTab === 'create-order' && (
          <div className="form-section">
            <h2>Create New Order</h2>
            <form onSubmit={handleCreateOrder}>
              <div className="form-group">
                <label>Amount (in paise):</label>
                <input
                  type="number"
                  value={orderForm.amount}
                  onChange={(e) => setOrderForm({ ...orderForm, amount: e.target.value })}
                  placeholder="50000"
                  required
                  min="100"
                />
                <small>₹{(orderForm.amount / 100).toFixed(2)}</small>
              </div>

              <div className="form-group">
                <label>Receipt ID:</label>
                <input
                  type="text"
                  value={orderForm.receipt}
                  onChange={(e) => setOrderForm({ ...orderForm, receipt: e.target.value })}
                  placeholder="receipt_123"
                  required
                />
              </div>

              <div className="form-group">
                <label>Notes (JSON):</label>
                <textarea
                  value={orderForm.notes}
                  onChange={(e) => setOrderForm({ ...orderForm, notes: e.target.value })}
                  placeholder='{"customer_name": "John Doe"}'
                  rows="3"
                />
              </div>

              <button type="submit" className="btn-primary">Create Order</button>
            </form>

            {createdOrder && (
              <div className="result-box success">
                <h3>✓ Order Created Successfully</h3>
                <pre>{JSON.stringify(createdOrder, null, 2)}</pre>
              </div>
            )}
          </div>
        )}

        {activeTab === 'create-payment' && (
          <div className="form-section">
            <h2>Create Payment</h2>
            <form onSubmit={handleCreatePayment}>
              <div className="form-group">
                <label>Order ID:</label>
                <input
                  type="text"
                  value={paymentForm.order_id}
                  onChange={(e) => setPaymentForm({ ...paymentForm, order_id: e.target.value })}
                  placeholder="order_xxxxxxxxx"
                  required
                />
              </div>

              <div className="form-group">
                <label>Payment Method:</label>
                <select
                  value={paymentForm.method}
                  onChange={(e) => setPaymentForm({ ...paymentForm, method: e.target.value })}
                >
                  <option value="upi">UPI</option>
                  <option value="card">Card</option>
                </select>
              </div>

              {paymentForm.method === 'upi' && (
                <div className="form-group">
                  <label>UPI VPA:</label>
                  <input
                    type="text"
                    value={paymentForm.vpa}
                    onChange={(e) => setPaymentForm({ ...paymentForm, vpa: e.target.value })}
                    placeholder="user@paytm"
                    required
                  />
                </div>
              )}

              {paymentForm.method === 'card' && (
                <>
                  <div className="form-group">
                    <label>Card Number:</label>
                    <input
                      type="text"
                      value={paymentForm.card.number}
                      onChange={(e) => setPaymentForm({
                        ...paymentForm,
                        card: { ...paymentForm.card, number: e.target.value }
                      })}
                      placeholder="4111111111111111"
                      required
                    />
                  </div>

                  <div className="form-row">
                    <div className="form-group">
                      <label>Expiry Month:</label>
                      <input
                        type="number"
                        value={paymentForm.card.expiry_month}
                        onChange={(e) => setPaymentForm({
                          ...paymentForm,
                          card: { ...paymentForm.card, expiry_month: e.target.value }
                        })}
                        placeholder="12"
                        min="1"
                        max="12"
                        required
                      />
                    </div>

                    <div className="form-group">
                      <label>Expiry Year:</label>
                      <input
                        type="number"
                        value={paymentForm.card.expiry_year}
                        onChange={(e) => setPaymentForm({
                          ...paymentForm,
                          card: { ...paymentForm.card, expiry_year: e.target.value }
                        })}
                        placeholder="2025"
                        required
                      />
                    </div>
                  </div>

                  <div className="form-row">
                    <div className="form-group">
                      <label>CVV:</label>
                      <input
                        type="text"
                        value={paymentForm.card.cvv}
                        onChange={(e) => setPaymentForm({
                          ...paymentForm,
                          card: { ...paymentForm.card, cvv: e.target.value }
                        })}
                        placeholder="123"
                        maxLength="4"
                        required
                      />
                    </div>

                    <div className="form-group">
                      <label>Cardholder Name:</label>
                      <input
                        type="text"
                        value={paymentForm.card.holder_name}
                        onChange={(e) => setPaymentForm({
                          ...paymentForm,
                          card: { ...paymentForm.card, holder_name: e.target.value }
                        })}
                        placeholder="John Doe"
                        required
                      />
                    </div>
                  </div>
                </>
              )}

              <button type="submit" className="btn-primary">Create Payment</button>
            </form>

            {createdPayment && (
              <div className={`result-box ${createdPayment.status === 'success' ? 'success' : createdPayment.status === 'failed' ? 'error' : 'info'}`}>
                <h3>
                  {createdPayment.status === 'processing' && '⏳ Payment Processing...'}
                  {createdPayment.status === 'success' && '✓ Payment Successful'}
                  {createdPayment.status === 'failed' && '✗ Payment Failed'}
                </h3>
                <pre>{JSON.stringify(createdPayment, null, 2)}</pre>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

export default Dashboard;
