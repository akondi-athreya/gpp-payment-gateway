import { useState, useEffect } from 'react';
import apiService from '../services/api';
import './Transactions.css';

function Transactions() {
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadPayments();
  }, []);

  const loadPayments = async () => {
    try {
      const data = await apiService.getPayments();
      setPayments(data);
      setLoading(false);
    } catch (err) {
      setError('Failed to load transactions: ' + err.message);
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleString('en-IN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  };

  if (loading) {
    return <div className="transactions-container"><div className="loading">Loading...</div></div>;
  }

  return (
    <div className="transactions-container">
      <header className="transactions-header">
        <h1>Transactions</h1>
      </header>

      {error && <div className="error-message">{error}</div>}

      <div className="transactions-content">
        <table data-test-id="transactions-table">
          <thead>
            <tr>
              <th>Payment ID</th>
              <th>Order ID</th>
              <th>Amount</th>
              <th>Method</th>
              <th>Status</th>
              <th>Created</th>
            </tr>
          </thead>
          <tbody>
            {payments.length === 0 ? (
              <tr>
                <td colSpan="6" style={{ textAlign: 'center', padding: '40px' }}>
                  No transactions found. Create an order and payment to see them here.
                </td>
              </tr>
            ) : (
              payments.map((payment) => (
                <tr key={payment.id} data-test-id="transaction-row" data-payment-id={payment.id}>
                  <td data-test-id="payment-id">{payment.id}</td>
                  <td data-test-id="order-id">{payment.order_id}</td>
                  <td data-test-id="amount">₹{(payment.amount / 100).toLocaleString('en-IN', { maximumFractionDigits: 2, minimumFractionDigits: 2 })}</td>
                  <td data-test-id="method">{payment.method}</td>
                  <td data-test-id="status">
                    <span className={`status-badge status-${payment.status}`}>
                      {payment.status}
                    </span>
                  </td>
                  <td data-test-id="created-at">{formatDate(payment.created_at)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default Transactions;
