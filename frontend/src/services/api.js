// API base URL from environment variable
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8000';

// Test merchant credentials
const TEST_API_KEY = 'key_test_abc123';
const TEST_API_SECRET = 'secret_test_xyz789';

/**
 * API service for Payment Gateway
 */
class ApiService {
  constructor() {
    this.baseURL = API_BASE_URL;
    this.apiKey = TEST_API_KEY;
    this.apiSecret = TEST_API_SECRET;
  }

  /**
   * Make authenticated API request
   */
  async request(endpoint, options = {}) {
    const url = `${this.baseURL}${endpoint}`;
    const headers = {
      'Content-Type': 'application/json',
      'X-Api-Key': this.apiKey,
      'X-Api-Secret': this.apiSecret,
      ...options.headers,
    };

    try {
      const response = await fetch(url, {
        ...options,
        headers,
      });

      const data = await response.json();

      if (!response.ok) {
        // Extract error message from nested error object or flat structure
        const errorMessage = data.error?.description || data.error_description || data.message || 'API request failed';
        throw new Error(errorMessage);
      }

      return data;
    } catch (error) {
      console.error('API Error:', error);
      throw error;
    }
  }

  /**
   * Get test merchant details
   */
  async getTestMerchant() {
    return this.request('/api/v1/test/merchant', {
      headers: {}, // No auth required
    });
  }

  /**
   * Create a new order
   */
  async createOrder(orderData) {
    return this.request('/api/v1/orders', {
      method: 'POST',
      body: JSON.stringify(orderData),
    });
  }

  /**
   * Get order by ID
   */
  async getOrder(orderId) {
    return this.request(`/api/v1/orders/${orderId}`);
  }

  /**
   * Get all payments for merchant
   */
  async getPayments() {
    return this.request('/api/v1/payments', {
      method: 'GET',
    });
  }

  /**
   * Create a payment
   */
  async createPayment(paymentData) {
    return this.request('/api/v1/payments', {
      method: 'POST',
      body: JSON.stringify(paymentData),
    });
  }

  /**
   * Get payment by ID
   */
  async getPayment(paymentId) {
    return this.request(`/api/v1/payments/${paymentId}`);
  }

  /**
   * Health check
   */
  async healthCheck() {
    return this.request('/health', {
      headers: {}, // No auth required
    });
  }
}

export default new ApiService();
