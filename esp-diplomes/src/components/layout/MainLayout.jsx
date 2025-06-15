// Components/layout/MainLayout.jsx - Mise en page principale
import React, { useState, useContext } from 'react';
import { useLocation } from 'react-router-dom';
import { AuthContext } from '../../context/AuthContext';
import { 
  Box, 
  CssBaseline, 
  AppBar, 
  Toolbar, 
  Typography, 
  Drawer, 
  List, 
  ListItemButton, 
  ListItemIcon, 
  ListItemText, 
  Divider,
  IconButton,
  Avatar,
  Menu,
  MenuItem,
  Tooltip
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import DashboardIcon from '@mui/icons-material/Dashboard';
import AssignmentIcon from '@mui/icons-material/Assignment';
import PeopleIcon from '@mui/icons-material/People';
import LogoutIcon from '@mui/icons-material/Logout';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import DownloadIcon from '@mui/icons-material/Download';
import NotificationsIcon from '@mui/icons-material/Notifications';
import { useNavigate } from 'react-router-dom';

const drawerWidth = 240;

const MainLayout = ({ children }) => {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [anchorEl, setAnchorEl] = useState(null);

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const handleMenuOpen = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  // Navigation items based on user role
  const studentNavItems = [
    { text: 'Tableau de bord', icon: <DashboardIcon />, path: '/student' },
    { text: 'État de validation', icon: <AssignmentIcon />, path: '/student/status' },
    { text: 'Documents', icon: <DownloadIcon />, path: '/student/documents' },
  ];

  const adminNavItems = [
    { text: 'Tableau de bord', icon: <DashboardIcon />, path: '/admin' },
    { text: 'Demandes de validation', icon: <AssignmentIcon />, path: '/admin/requests' },
  ];

  const navItems = user?.role === 'student' ? studentNavItems : adminNavItems;

  const drawer = (
    <div>
      <Toolbar sx={{ justifyContent: 'center' }}>
        <Typography variant="h6" noWrap component="div">
          ESP Diplômes
        </Typography>
      </Toolbar>
      <Divider />
      <List>
        {navItems.map((item) => (
          <ListItemButton
            key={item.text}
            selected={location.pathname === item.path}
            onClick={() => {
              navigate(item.path);
              setMobileOpen(false);
            }}
          >
            <ListItemIcon>{item.icon}</ListItemIcon>
            <ListItemText primary={item.text} />
          </ListItemButton>
        ))}
      </List>
    </div>
  );

  return (
    <Box sx={{ display: 'flex' }}>
      <CssBaseline />
      <AppBar
        position="fixed"
        sx={{
          width: { sm: `calc(100% - ${drawerWidth}px)` },
          ml: { sm: `${drawerWidth}px` },
        }}
      >
        <Toolbar>
          <IconButton
            color="inherit"
            aria-label="open drawer"
            edge="start"
            onClick={handleDrawerToggle}
            sx={{ mr: 2, display: { sm: 'none' } }}
          >
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
            Système de Validation des Diplômes
          </Typography>
          <Tooltip title="Notifications">
            <IconButton color="inherit">
              <NotificationsIcon />
            </IconButton>
          </Tooltip>
          <Tooltip title={user?.name || 'Profil'}>
            <IconButton
              onClick={handleMenuOpen}
              size="large"
              edge="end"
              color="inherit"
            >
              <Avatar sx={{ width: 32, height: 32, bgcolor: 'secondary.main' }}>
                {user?.name?.charAt(0) || 'U'}
              </Avatar>
            </IconButton>
          </Tooltip>
          <Menu
            anchorEl={anchorEl}
            open={Boolean(anchorEl)}
            onClose={handleMenuClose}
            anchorOrigin={{
              vertical: 'bottom',
              horizontal: 'right',
            }}
            transformOrigin={{
              vertical: 'top',
              horizontal: 'right',
            }}
          >
            <MenuItem onClick={() => {
              handleMenuClose();
              // Navigate to profile page
            }}>
              <ListItemIcon>
                <AccountCircleIcon fontSize="small" />
              </ListItemIcon>
              <ListItemText>Mon profil</ListItemText>
            </MenuItem>
            <Divider />
            <MenuItem onClick={handleLogout}>
              <ListItemIcon>
                <LogoutIcon fontSize="small" />
              </ListItemIcon>
              <ListItemText>Déconnexion</ListItemText>
            </MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>
      <Box
        component="nav"
        sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}
      >
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={handleDrawerToggle}
          ModalProps={{
            keepMounted: true, // Better mobile performance
          }}
          sx={{
            display: { xs: 'block', sm: 'none' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
          }}
        >
          {drawer}
        </Drawer>
        <Drawer
          variant="permanent"
          sx={{
            display: { xs: 'none', sm: 'block' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
          }}
          open
        >
          {drawer}
        </Drawer>
      </Box>
      <Box
        component="main"
        sx={{ 
          flexGrow: 1, 
          p: 3, 
          width: { sm: `calc(100% - ${drawerWidth}px)` },
          minHeight: '100vh',
          backgroundColor: 'background.default'
        }}
      >
        <Toolbar /> {/* This toolbar provides spacing below the AppBar */}
        {children}
      </Box>
    </Box>
  );
};

export default MainLayout;