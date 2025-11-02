import { useState } from 'react'
import {
  Container,
  Tabs,
  Tab,
  Box,
  TextField,
  Button,
  Typography,
  Alert,
  Paper,
  CircularProgress,
} from '@mui/material'
import { login, register } from '../services/authentication'
import { useNavigate, Link } from 'react-router-dom'

const AuthPage = () => {
  const navigate = useNavigate()
  const [role, setRole] = useState('CITIZEN') // or "STAFF"
  const [isLogin, setIsLogin] = useState(true)
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value })
    // Clear error when user starts typing
    if (error) setError('')
  }

  const handleRoleChange = (e, newRole) => {
    setRole(newRole)
    setError('')
  }

  const handleToggleMode = () => {
    setIsLogin(!isLogin)
    setError('')
    setFormData({ name: '', email: '', password: '' })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')

    try {
      const credentials = {
        email: formData.email,
        password: formData.password,
      }

      let response

      if (isLogin) {
        response = await login(credentials)
      } else {
        response = await register({ ...credentials, name: formData.name }, role)
      }

      console.log(response)
      localStorage.setItem('token', response.token)
      localStorage.setItem('role', response.role || role)

      // Navigate based on the role from response or selected role
      const userRole = response.role || role
      if (userRole === 'CITIZEN') {
        navigate('/citizen/dashboard')
      } else {
        navigate('/staff/dashboard')
      }
    } catch (error) {
      console.error('Auth error:', error)

      // Extract error message
      let errorMessage = 'Authentication failed. Please try again.'

      setError(errorMessage)
    } finally {
      setLoading(false)
    }
  }

  return (
    <Container maxWidth="xs" sx={{ mt: 8 }}>
      <Button
        variant="outlined"
        size="small"
        sx={{ position: 'absolute', top: 16, left: 16 }}
        component={Link}
        to="/"
      >
        ← Home
      </Button>

      <Paper elevation={3} sx={{ p: 4, mt: 4 }}>
        <Typography variant="h4" align="center" gutterBottom>
          {isLogin ? 'Welcome Back' : 'Create Account'}
        </Typography>

        <Typography
          variant="body2"
          align="center"
          color="text.secondary"
          sx={{ mb: 3 }}
        >
          {isLogin
            ? 'Sign in to access your dashboard'
            : 'Register to start booking collections'}
        </Typography>

        <Tabs value={role} onChange={handleRoleChange} centered sx={{ mb: 3 }}>
          <Tab label="Citizen" value="CITIZEN" />
          <Tab label="Staff" value="STAFF" />
        </Tabs>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>
            {error}
          </Alert>
        )}

        <Box component="form" onSubmit={handleSubmit}>
          {!isLogin && (
            <TextField
              fullWidth
              label="Full Name"
              name="name"
              margin="normal"
              value={formData.name}
              onChange={handleChange}
              required
              disabled={loading}
              autoComplete="name"
            />
          )}

          <TextField
            fullWidth
            label="Email Address"
            name="email"
            type="email"
            margin="normal"
            value={formData.email}
            onChange={handleChange}
            required
            disabled={loading}
            autoComplete="email"
          />

          <TextField
            fullWidth
            label="Password"
            name="password"
            type="password"
            margin="normal"
            value={formData.password}
            onChange={handleChange}
            required
            disabled={loading}
            autoComplete={isLogin ? 'current-password' : 'new-password'}
          />

          <Button
            fullWidth
            type="submit"
            variant="contained"
            size="large"
            sx={{ mt: 3, mb: 2 }}
            disabled={loading}
            startIcon={
              loading && <CircularProgress size={20} color="inherit" />
            }
          >
            {loading
              ? 'Please wait...'
              : isLogin
                ? 'Sign In'
                : 'Create Account'}
          </Button>

          <Button
            fullWidth
            variant="text"
            onClick={handleToggleMode}
            disabled={loading}
          >
            {isLogin
              ? "Don't have an account? Register"
              : 'Already have an account? Sign In'}
          </Button>
        </Box>
      </Paper>

      <Typography
        variant="caption"
        display="block"
        align="center"
        color="text.secondary"
        sx={{ mt: 3 }}
      >
        {role === 'CITIZEN'
          ? 'Book waste collection appointments'
          : 'Manage and approve bookings'}
      </Typography>
    </Container>
  )
}

export default AuthPage
