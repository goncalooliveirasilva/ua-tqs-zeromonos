import { useState } from 'react'
import {
  Container,
  Tabs,
  Tab,
  Box,
  TextField,
  Button,
  Typography,
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

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)

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
      localStorage.setItem('role', role)

      if (role === 'CITIZEN') {
        navigate('/citizen/dashboard')
      } else {
        navigate('/staff/dashboard')
      }
    } catch (error) {
      console.error('Auth error:', error)
      alert(error.response?.data?.error || 'Authentication failed')
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
        Home
      </Button>
      <Typography variant="h4" align="center" gutterBottom>
        {isLogin ? 'Login' : 'Register'}
      </Typography>

      <Tabs
        value={role}
        onChange={(e, newRole) => setRole(newRole)}
        centered
        sx={{ mb: 3 }}
      >
        <Tab label="Citizen" value="CITIZEN" />
        <Tab label="Staff" value="STAFF" />
      </Tabs>

      <Box component="form" onSubmit={handleSubmit}>
        {!isLogin && (
          <TextField
            fullWidth
            label="Name"
            name="name"
            margin="normal"
            onChange={handleChange}
            required
          />
        )}
        <TextField
          fullWidth
          label="Email"
          name="email"
          type="email"
          margin="normal"
          onChange={handleChange}
          required
        />
        <TextField
          fullWidth
          label="Password"
          name="password"
          type="password"
          margin="normal"
          onChange={handleChange}
          required
        />

        <Button
          fullWidth
          type="submit"
          variant="contained"
          sx={{ mt: 2 }}
          disabled={loading}
        >
          {loading ? 'Please wait...' : isLogin ? 'Login' : 'Register'}
        </Button>

        <Button
          fullWidth
          variant="text"
          sx={{ mt: 1 }}
          onClick={() => setIsLogin(!isLogin)}
        >
          {isLogin
            ? "Don't have an account? Register"
            : 'Already have an account? Login'}
        </Button>
      </Box>
    </Container>
  )
}

export default AuthPage
