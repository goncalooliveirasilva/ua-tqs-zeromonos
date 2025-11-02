import { BrowserRouter, Routes, Route } from 'react-router-dom'
import LandingPage from './components/LandingPage'
import AuthPage from './components/AuthPage'
import CitizenDashboard from './components/CitizenDashboard'
import StaffDashboard from './components/StaffDashboard'

const App = () => (
  <BrowserRouter>
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route path="/login" element={<AuthPage />} />
      <Route path="/citizen/dashboard" element={<CitizenDashboard />} />
      <Route path="/staff/dashboard" element={<StaffDashboard />} />
    </Routes>
  </BrowserRouter>
)

export default App
