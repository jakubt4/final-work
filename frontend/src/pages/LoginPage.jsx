import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Input from '../components/Input';
import Button from '../components/Button';
import Alert from '../components/Alert';

export default function LoginPage() {
  const navigate = useNavigate();
  const { login, isAuthenticated } = useAuth();

  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });
  const [errors, setErrors] = useState({});
  const [apiError, setApiError] = useState('');
  const [loading, setLoading] = useState(false);

  // Redirect if already authenticated
  if (isAuthenticated) {
    navigate('/products', { replace: true });
    return null;
  }

  const validateForm = () => {
    const newErrors = {};

    if (!formData.email.trim()) {
      newErrors.email = 'Email is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Please enter a valid email address';
    }

    if (!formData.password) {
      newErrors.password = 'Password is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    // Clear field error when user starts typing
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
    // Clear API error when user modifies form
    if (apiError) {
      setApiError('');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setApiError('');

    if (!validateForm()) {
      return;
    }

    setLoading(true);

    try {
      await login(formData.email, formData.password);
      navigate('/products', { replace: true });
    } catch (error) {
      if (error.response) {
        const status = error.response.status;
        if (status === 401 || status === 403) {
          setApiError('Invalid email or password');
        } else if (status >= 500) {
          setApiError('Something went wrong. Please try again later.');
        } else {
          setApiError(
            error.response.data?.message || 'Login failed. Please try again.'
          );
        }
      } else if (error.request) {
        setApiError('Unable to connect to server. Please check your connection.');
      } else {
        setApiError('An unexpected error occurred. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  const fillDemoCredentials = () => {
    setFormData({
      email: 'user@example.com',
      password: 'password',
    });
    setErrors({});
    setApiError('');
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div className="text-center">
          <h1 className="text-3xl font-bold text-blue-600">GPU Store</h1>
          <h2 className="mt-4 text-xl font-semibold text-gray-900">
            Sign in to your account
          </h2>
        </div>

        <div className="bg-white rounded-lg shadow-md p-8">
          {apiError && (
            <Alert
              type="error"
              message={apiError}
              className="mb-4"
              dismissible
              onDismiss={() => setApiError('')}
            />
          )}

          <form onSubmit={handleSubmit} className="space-y-6">
            <Input
              label="Email"
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="Enter your email"
              error={errors.email}
              required
              disabled={loading}
            />

            <Input
              label="Password"
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="Enter your password"
              error={errors.password}
              required
              disabled={loading}
            />

            <Button
              type="submit"
              variant="primary"
              loading={loading}
              disabled={loading}
              className="w-full"
            >
              {loading ? 'Signing in...' : 'Sign in'}
            </Button>
          </form>

          <div className="mt-4 text-center">
            <button
              type="button"
              onClick={fillDemoCredentials}
              className="text-sm text-blue-600 hover:text-blue-800 underline"
              disabled={loading}
            >
              Use demo credentials
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
