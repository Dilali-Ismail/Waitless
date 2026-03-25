export interface User {
  userId: string;
  name: string;
  email: string;
  phoneNumber?: string;
  score?: number;
  status: 'ACTIVE' | 'SUSPENDED' | 'BANNED';
  suspensionEndDate?: string;
  ticketsCreated?: number;
  ticketsServed?: number;
  ticketsCancelled?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface Company {
  id?: number;
  name: string;
  category: string;
  address?: string;
  phoneNumber?: string;
  email: string;
  status?: 'PENDING' | 'ACTIVE' | 'SUSPENDED';
  createdAt?: string;
  updatedAt?: string;
}

export interface Queue {
  id?: number;
  name: string;
  description?: string;
  capacity: number;
  averageServiceTime: number;
  isActive?: boolean;
  openingTime?: string;
  closingTime?: string;
  companyId: number;
  companyName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Counter {
  id?: number;
  counterNumber: number;
  isActive?: boolean;
  queueId: number;
  queueName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Ticket {
  id?: number;
  queueId: number;
  userId: string;
  clientName: string;
  position?: number;
  status: 'WAITING' | 'CALLED' | 'COMPLETED' | 'ABSENT' | 'CANCELLED';
  estimatedWaitTime?: number;
  counterNumber?: number;
  calledAt?: string;
  completedAt?: string;
  createdAt?: string;
}

export interface CreateTicketRequest {
  queueId: number;
  userId: string;
  clientName: string;
  estimatedWaitTime?: number;
}

export interface Estimation {
  queueId: number;
  position: number;
  estimatedWaitTime: number;
  message?: string;
}
