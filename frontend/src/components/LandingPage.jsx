import { useState } from 'react'
import { Link } from 'react-router-dom'
import {
  Button,
  Container,
  Typography,
  TextField,
  Box,
  Paper,
  Grid,
  Chip,
  Divider,
  Stack,
  Card,
  CardContent,
  Alert,
  CircularProgress,
} from '@mui/material'
import {
  CalendarToday,
  AccessTime,
  LocationOn,
  Description,
  CheckCircle,
  Cancel,
  HourglassEmpty,
  Info,
} from '@mui/icons-material'
import axios from 'axios'

const LandingPage = () => {
  const [token, setToken] = useState('')
  const [booking, setBooking] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const fetchBooking = async () => {
    if (!token.trim()) {
      setError('Please enter a booking token')
      return
    }

    setLoading(true)
    setError('')
    setBooking(null)

    try {
      const res = await axios.get(
        `http://localhost:8080/api/v1/bookings/public/${token}`,
      )
      setBooking(res.data)
      setError('')
    } catch (err) {
      console.log(err)
      setBooking(null)
      if (err.response?.status === 404) {
        setError('Booking not found. Please check your token and try again.')
      } else {
        setError('An error occurred. Please try again.')
      }
    } finally {
      setLoading(false)
    }
  }

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      fetchBooking()
    }
  }

  const getStateInfo = (state) => {
    switch (state) {
      case 'RECEIVED':
        return {
          color: 'info',
          icon: <HourglassEmpty />,
          label: 'Received',
          gradient: 'linear-gradient(135deg, #2196f3 0%, #64b5f6 100%)', // Blue
        }

      case 'ASSIGNED':
        return {
          color: 'secondary',
          icon: <AssignmentInd />,
          label: 'Assigned',
          gradient: 'linear-gradient(135deg, #9c27b0 0%, #ba68c8 100%)',
        }

      case 'IN_PROGRESS':
        return {
          color: 'warning',
          icon: <Build />,
          label: 'In Progress',
          gradient: 'linear-gradient(135deg, #ff9800 0%, #ffb74d 100%)',
        }

      case 'DONE':
        return {
          color: 'success',
          icon: <CheckCircle />,
          label: 'Done',
          gradient: 'linear-gradient(135deg, #4caf50 0%, #81c784 100%)',
        }

      case 'CANCELED':
        return {
          color: 'error',
          icon: <Cancel />,
          label: 'Canceled',
          gradient: 'linear-gradient(135deg, #f44336 0%, #e57373 100%)',
        }

      default:
        return {
          color: 'default',
          icon: <Info />,
          label: state || 'Unknown',
          gradient: 'linear-gradient(135deg, #9e9e9e 0%, #bdbdbd 100%)',
        }
    }
  }

  const stateInfo = booking ? getStateInfo(booking.state) : null

  return (
    <Container maxWidth="lg" sx={{ py: 8 }}>
      {/* Hero Section */}
      <Box sx={{ textAlign: 'center', mb: 8 }}>
        <Typography
          variant="h2"
          component="h1"
          gutterBottom
          sx={{
            fontWeight: 700,
            background: 'linear-gradient(45deg, #2196F3 30%, #21CBF3 90%)',
            backgroundClip: 'text',
            textFillColor: 'transparent',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
          }}
        >
          ZeroMonos Collection
        </Typography>
        <Typography
          variant="h5"
          color="text.secondary"
          gutterBottom
          sx={{ mb: 4 }}
        >
          Book and manage waste collection easily
        </Typography>
        <Button
          id="get-started-button"
          component={Link}
          to="/login"
          variant="contained"
          size="large"
          sx={{
            px: 4,
            py: 1.5,
            fontSize: '1.1rem',
            borderRadius: 2,
            textTransform: 'none',
          }}
        >
          Get Started
        </Button>
      </Box>

      {/* Booking Lookup Section */}
      <Paper
        elevation={3}
        sx={{
          p: 4,
          maxWidth: 600,
          mx: 'auto',
          borderRadius: 3,
        }}
      >
        <Typography
          variant="h5"
          gutterBottom
          align="center"
          sx={{ mb: 3, fontWeight: 600 }}
        >
          Check Your Booking
        </Typography>

        <Stack direction="row" spacing={2} sx={{ mb: 2 }}>
          <TextField
            fullWidth
            label="Booking Token"
            id="booking-token-input"
            placeholder="Enter your booking token"
            value={token}
            onChange={(e) => {
              setToken(e.target.value)
              setError('')
            }}
            onKeyPress={handleKeyPress}
            disabled={loading}
            error={!!error}
          />
          <Button
            id="booking-token-button"
            variant="contained"
            onClick={fetchBooking}
            disabled={loading || !token.trim()}
            sx={{ minWidth: 120 }}
          >
            {loading ? <CircularProgress size={24} /> : 'Lookup'}
          </Button>
        </Stack>

        {error && (
          <Alert severity="error" sx={{ mt: 2 }}>
            {error}
          </Alert>
        )}
      </Paper>

      {/* Booking Details */}
      {booking && (
        <Card
          elevation={4}
          sx={{
            mt: 4,
            maxWidth: 800,
            mx: 'auto',
            borderRadius: 3,
            overflow: 'hidden',
          }}
        >
          {/* Status Header */}
          <Box
            sx={{
              background: stateInfo.gradient,
              p: 3,
              color: 'white',
            }}
          >
            <Stack
              direction="row"
              alignItems="center"
              justifyContent="space-between"
            >
              <Typography variant="h5" sx={{ fontWeight: 600 }}>
                Booking Details
              </Typography>
              <Chip
                icon={stateInfo.icon}
                label={stateInfo.label}
                sx={{
                  bgcolor: 'rgba(255,255,255,0.3)',
                  color: 'white',
                  fontWeight: 600,
                  '& .MuiChip-icon': { color: 'white' },
                }}
              />
            </Stack>
          </Box>

          <CardContent sx={{ p: 4 }} id="booking-card">
            <Grid container spacing={4}>
              {/* location*/}
              <Grid item xs={12}>
                <Typography
                  variant="h6"
                  gutterBottom
                  sx={{ display: 'flex', alignItems: 'center', gap: 1 }}
                >
                  <LocationOn color="primary" />
                  Location
                </Typography>
                <Divider sx={{ mb: 2 }} />

                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                  {booking.district && (
                    <Typography variant="body1">
                      <strong>District:</strong> {booking.district}
                    </Typography>
                  )}
                  <Typography variant="body1">
                    <strong>Municipality:</strong> {booking.municipality}
                  </Typography>
                  <Typography variant="body1">
                    <strong>Village:</strong> {booking.village}
                  </Typography>
                  <Typography variant="body1">
                    <strong>Postal Code:</strong> {booking.postalCode}
                  </Typography>
                </Box>
              </Grid>

              {/* schedule */}
              <Grid item xs={12}>
                <Typography
                  variant="h6"
                  gutterBottom
                  sx={{ display: 'flex', alignItems: 'center', gap: 1 }}
                >
                  <CalendarToday color="primary" />
                  Schedule
                </Typography>
                <Divider sx={{ mb: 2 }} />

                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                  <Typography variant="body1">
                    <strong>Date:</strong>{' '}
                    {new Date(booking.date).toLocaleDateString('en-US', {
                      weekday: 'long',
                      year: 'numeric',
                      month: 'long',
                      day: 'numeric',
                    })}
                  </Typography>
                  <Typography variant="body1" sx={{ display: 'flex', gap: 1 }}>
                    <strong>Time:</strong> <AccessTime fontSize="small" />{' '}
                    {booking.time}
                  </Typography>
                </Box>
              </Grid>

              {/* description */}
              <Grid item xs={12}>
                <Typography
                  variant="h6"
                  gutterBottom
                  sx={{ display: 'flex', alignItems: 'center', gap: 1 }}
                >
                  <Description color="primary" />
                  Description
                </Typography>
                <Divider sx={{ mb: 2 }} />

                <Paper
                  variant="outlined"
                  sx={{ p: 2, bgcolor: 'grey.50', borderRadius: 2 }}
                >
                  <Typography variant="body1" sx={{ whiteSpace: 'pre-line' }}>
                    {booking.description}
                  </Typography>
                </Paper>
              </Grid>
            </Grid>
          </CardContent>
        </Card>
      )}

      {/* Info Section */}
      {!booking && !error && (
        <Box sx={{ mt: 6, textAlign: 'center' }}>
          <Typography variant="body1" color="text.secondary">
            Enter your booking token above to view your appointment details
          </Typography>
        </Box>
      )}
    </Container>
  )
}

export default LandingPage
