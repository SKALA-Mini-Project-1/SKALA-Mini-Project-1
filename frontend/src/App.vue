<script setup lang="ts">
import { reactive, ref } from 'vue';
import Header from './components/Header.vue';
import ConcertDetail from './components/ConcertDetail.vue';
import QueueScreen from './components/QueueScreen.vue';
import SeatSelection from './components/SeatSelection.vue';
import PaymentScreen from './components/PaymentScreen.vue';
import BookingConfirmation from './components/BookingConfirmation.vue';
import MyPage from './components/MyPage.vue';
import ServerError from './components/ServerError.vue';
import SoldOut from './components/SoldOut.vue';
import type { BookingData, Seat, Step } from './types';

const currentStep = ref<Step>('detail');
const bookingData = reactive<BookingData>({
  date: null,
  session: null,
  seats: []
});

const handleBookingStart = (date: string, session: string) => {
  bookingData.date = date;
  bookingData.session = session;
  currentStep.value = 'queue';
};

const handleQueueComplete = () => {
  currentStep.value = 'seat';
};

const handleSeatComplete = (seats: Seat[]) => {
  bookingData.seats = seats;
  currentStep.value = 'payment';
};

const handlePaymentComplete = () => {
  currentStep.value = 'confirm';
};

const navigate = (step: Step) => {
  currentStep.value = step;
};
</script>

<template>
  <div class="min-h-screen bg-white font-sans text-[#333]">
    <Header :current-step="currentStep" @navigate="navigate" />
    <main>
      <ConcertDetail
        v-if="currentStep === 'detail'"
        @booking-start="handleBookingStart"
      />
      <QueueScreen v-else-if="currentStep === 'queue'" @queue-complete="handleQueueComplete" />
      <SeatSelection v-else-if="currentStep === 'seat'" @complete="handleSeatComplete" />
      <PaymentScreen
        v-else-if="currentStep === 'payment'"
        :booking-data="bookingData"
        @payment-complete="handlePaymentComplete"
      />
      <BookingConfirmation
        v-else-if="currentStep === 'confirm'"
        :booking-data="bookingData"
        @navigate="navigate"
      />
      <MyPage v-else-if="currentStep === 'mypage'" />
      <ServerError v-else-if="currentStep === 'error'" @retry="navigate('detail')" />
      <SoldOut v-else-if="currentStep === 'soldout'" @back="navigate('detail')" />
      <ConcertDetail v-else @booking-start="handleBookingStart" />
    </main>

    <footer class="mt-10 border-t border-[#e0e0e0] bg-[#f5f5f5] py-6 md:mt-12 md:py-8">
      <div class="mx-auto max-w-[1200px] px-3 text-xs text-[#999] sm:px-4">
        <div class="mb-4 flex flex-wrap gap-x-4 gap-y-1 font-bold text-[#666]">
          <span>회사소개</span>
          <span>이용약관</span>
          <span>개인정보처리방침</span>
          <span>청소년보호정책</span>
          <span>고객센터</span>
        </div>
        <p class="leading-relaxed">
          (주)티켓코리아 | 대표이사: 김철수 | 사업자등록번호: 123-45-67890
          <br />
          주소: 서울특별시 강남구 테헤란로 123 | 통신판매업신고: 2025-서울강남-00000
          <br />
          고객센터: 1544-0000 (평일 09:00~18:00) | 팩스: 02-0000-0000 | 이메일: help@ticketkorea.com
        </p>
        <p class="mt-4 text-[#ccc]">Copyright © TICKET KOREA Corp. All Rights Reserved.</p>
      </div>
    </footer>
  </div>
</template>
