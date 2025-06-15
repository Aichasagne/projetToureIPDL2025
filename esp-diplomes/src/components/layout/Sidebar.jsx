"use client";

import React, { useContext } from 'react';
import { AuthContext } from '../../context/AuthContext';
import Link from 'next/link';
import {
  Drawer,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Divider,
  Box,
  Typography,
} from '@mui/material';
import {
  Dashboard as DashboardIcon,
  Assignment as AssignmentIcon,
  Description as DescriptionIcon,
  People as PeopleIcon,
  Settings as SettingsIcon,
} from '@mui/icons-material';

const Sidebar = ({ open, onClose }) => {
  const { user } = useContext(AuthContext);

  // Définir les éléments de navigation en fonction du rôle de l'utilisateur
  const navItems = user && user.role === 'student' ? [
    { text: 'Tableau de bord', path: '/student', icon: <DashboardIcon /> },
    { text: 'État de validation', path: '/student/status', icon: <AssignmentIcon /> },
    { text: 'Documents', path: '/student/documents', icon: <DescriptionIcon /> },
  ] : user && user.role === 'admin' ? [
    { text: 'Tableau de bord', path: '/admin', icon: <DashboardIcon /> },
    { text: 'Demandes de validation', path: '/admin/requests', icon: <AssignmentIcon /> },
    { text: 'Gestion des utilisateurs', path: '/admin/users', icon: <PeopleIcon /> },
    { text: 'Paramètres', path: '/admin/settings', icon: <SettingsIcon /> },
  ] : [];

  return (
    <Drawer
      anchor="left"
      open={open}
      onClose={onClose}
      sx={{
        width: 240,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width: 240,
          boxSizing: 'border-box',
        },
      }}
    >
      <Box sx={{ p: 2 }}>
        <Typography variant="h6" noWrap component="div">
          Système de Validation des Diplômes
        </Typography>
      </Box>
      <Divider />
      <List>
        {navItems.map((item) => (
          <ListItem
            button
            key={item.text}
            component={Link}
            href={item.path}
            onClick={onClose} // Ferme le tiroir sur mobile après clic
          >
            <ListItemIcon>{item.icon}</ListItemIcon>
            <ListItemText primary={item.text} />
          </ListItem>
        ))}
      </List>
      <Divider />
    </Drawer>
  );
};

export default Sidebar;